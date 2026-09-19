#!/usr/bin/env node

/**
 * Thmanyah Font Fetcher — خط ثمانية (Android)
 *
 * Puts the five Thmanyah **Serif Text** weights into
 * `core/designsystem/.thmanyah/` as TTF, which is what `res/font` accepts —
 * the web ships woff2, Android cannot read it.
 *
 * WHY FETCH INSTEAD OF COMMIT: the Thmanyah license
 * (font.thmanyah.com/licenses) permits embedding in websites and apps,
 * including commercially, but forbids redistribution — the files may only be
 * downloaded from the official site. `databayt/android-app` is public, so the
 * faces must never land in git. The Gradle build copies whatever this script
 * left behind into a generated resource directory under `build/`, and falls
 * back to Noto Sans Arabic when it finds nothing, so a clean checkout with no
 * network still builds and still renders Arabic.
 *
 * Serif Text — not Sans, not Serif Display — is the face the web actually
 * sets the school dashboard in: `hogwarts/src/app/globals.css` maps
 * `--font-sans` to `var(--font-thmanyah-text)` under `:root[dir="rtl"]`.
 *
 * Two sources, in order:
 *   1. `~/Library/Fonts/thmanyah-serif-text-*.ttf` — already unwrapped on this
 *      Mac for Figma; no network, exact same outlines.
 *   2. The official specimen host, converted woff2 → TTF with fontTools
 *      (`pip install fonttools brotli`).
 *
 * Idempotent: weights already present are left alone. A 404 means Thmanyah
 * shipped a new generation — refresh MANIFEST from the @font-face rules on
 * https://font.thmanyah.com and bump MANIFEST_VERSION.
 */
import { execFileSync } from "child_process"
import fs from "fs"
import os from "os"
import path from "path"
import { fileURLToPath } from "url"

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const outDir = path.resolve(__dirname, "..", "core", "designsystem", ".thmanyah")

// Pinned from the @font-face rules on font.thmanyah.com (2026-07-13), ORIGINAL
// generation — the one the specimen site itself typesets with. Kept in sync
// with hogwarts `scripts/fetch-thmanyah.mjs`; only the serif-text family is
// listed here because only it reaches the UI.
const MANIFEST_VERSION = "original-2026-07-13"
const MANIFEST = {
  "thmanyah-serif-text-300":
    "https://framerusercontent.com/assets/pXAhTrUNoEDB4CTUcjXvXy1gFAc.woff2",
  "thmanyah-serif-text-400":
    "https://framerusercontent.com/assets/3WPjrTAizPcMoDaKDtKdNB0Jo50.woff2",
  "thmanyah-serif-text-500":
    "https://framerusercontent.com/assets/bsVNF5mKouPfUJiPWsBKDU324NM.woff2",
  "thmanyah-serif-text-700":
    "https://framerusercontent.com/assets/L62MixAFhJojtLTRD19wOsCnFg.woff2",
  "thmanyah-serif-text-900":
    "https://framerusercontent.com/assets/z8brS6Wjld2pvgVBkmzMQpyPO4.woff2",
}

/** Unwraps a woff2 to a plain TTF losslessly — no outline is touched. */
function unwrap(woff2Path, ttfPath) {
  execFileSync("python3", [
    "-c",
    [
      "import sys",
      "from fontTools.ttLib import TTFont",
      "f = TTFont(sys.argv[1])",
      "f.flavor = None",
      "f.save(sys.argv[2])",
    ].join("\n"),
    woff2Path,
    ttfPath,
  ])
}

async function main() {
  fs.mkdirSync(outDir, { recursive: true })

  // A generation bump invalidates files fetched under the same names.
  const versionFile = path.join(outDir, ".manifest-version")
  const current = fs.existsSync(versionFile)
    ? fs.readFileSync(versionFile, "utf8").trim()
    : ""
  if (current !== MANIFEST_VERSION) {
    for (const file of fs.readdirSync(outDir)) {
      if (file.endsWith(".ttf")) fs.unlinkSync(path.join(outDir, file))
    }
    fs.writeFileSync(versionFile, MANIFEST_VERSION)
  }

  const desktop = path.join(os.homedir(), "Library", "Fonts")
  const missing = []

  for (const name of Object.keys(MANIFEST)) {
    const dest = path.join(outDir, `${name}.ttf`)
    if (fs.existsSync(dest)) continue

    const local = path.join(desktop, `${name}.ttf`)
    if (fs.existsSync(local)) {
      fs.copyFileSync(local, dest)
      console.log(`📁 ${name}.ttf — copied from ~/Library/Fonts`)
      continue
    }
    missing.push(name)
  }

  if (!missing.length) {
    console.log(`✅ Thmanyah Serif Text present (${Object.keys(MANIFEST).length} weights)`)
    return
  }

  console.log(`⬇️  Fetching ${missing.length} weight(s) from the official host…`)
  for (const name of missing) {
    const url = MANIFEST[name]
    const res = await fetch(url)
    if (!res.ok) {
      throw new Error(
        `${name}: HTTP ${res.status} — Thmanyah likely shipped a new generation; ` +
          `refresh MANIFEST from https://font.thmanyah.com`
      )
    }
    const woff2 = path.join(outDir, `${name}.woff2`)
    fs.writeFileSync(woff2, Buffer.from(await res.arrayBuffer()))
    try {
      unwrap(woff2, path.join(outDir, `${name}.ttf`))
      console.log(`✅ ${name}.ttf`)
    } catch (error) {
      throw new Error(
        `${name}: woff2 → TTF failed. Install the converter with ` +
          `\`pip install fonttools brotli\`.\n${error.message}`
      )
    } finally {
      fs.unlinkSync(woff2)
    }
  }
}

main().catch((error) => {
  console.error(`❌ ${error.message}`)
  process.exit(1)
})
