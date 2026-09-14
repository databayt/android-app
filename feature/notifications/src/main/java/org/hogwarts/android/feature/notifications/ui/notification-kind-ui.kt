package org.hogwarts.android.feature.notifications.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector
import org.hogwarts.android.feature.notifications.R
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind.*

/** `config.ts` icons (lucide) as their Material look-alikes. */
internal fun NotificationKind?.icon(): ImageVector = when (this) {
    Message, MessageMention -> Icons.Outlined.ChatBubbleOutline
    AssignmentCreated -> Icons.AutoMirrored.Outlined.MenuBook
    AssignmentDue, EventReminder, ClassRescheduled, AbsenceIntention, AbsenceIntentionDecision -> Icons.Outlined.CalendarToday
    AssignmentGraded, GradePosted, DocumentShared, ReportReady -> Icons.Outlined.Description
    AttendanceMarked -> Icons.Outlined.HowToReg
    AttendanceAlert, FeeOverdue, ClassCancelled, AbsenceUnreportedFollowup, LiveClassCancelled, ContentRejected -> Icons.Outlined.WarningAmber
    FeeDue, FeePaid -> Icons.Outlined.AttachMoney
    Announcement -> Icons.Outlined.Campaign
    SystemAlert, null -> Icons.Outlined.Notifications
    AccountCreated, PasswordReset, LoginAlert -> Icons.Outlined.Shield
    SetupGuide, ContentReview -> Icons.Outlined.Checklist
    LiveClassScheduled, LiveClassStartingSoon, LiveClassStarted, LiveClassRecordingReady -> Icons.Outlined.Videocam
    ContentApproved -> Icons.Outlined.CheckCircle
}

/** `dictionary.notifications.types`. */
@StringRes
internal fun NotificationKind.labelRes(): Int = when (this) {
    Message -> R.string.notifications_type_message
    MessageMention -> R.string.notifications_type_message_mention
    AssignmentCreated -> R.string.notifications_type_assignment_created
    AssignmentDue -> R.string.notifications_type_assignment_due
    AssignmentGraded -> R.string.notifications_type_assignment_graded
    GradePosted -> R.string.notifications_type_grade_posted
    AttendanceMarked -> R.string.notifications_type_attendance_marked
    AttendanceAlert -> R.string.notifications_type_attendance_alert
    FeeDue -> R.string.notifications_type_fee_due
    FeeOverdue -> R.string.notifications_type_fee_overdue
    FeePaid -> R.string.notifications_type_fee_paid
    Announcement -> R.string.notifications_type_announcement
    EventReminder -> R.string.notifications_type_event_reminder
    ClassCancelled -> R.string.notifications_type_class_cancelled
    ClassRescheduled -> R.string.notifications_type_class_rescheduled
    SystemAlert -> R.string.notifications_type_system_alert
    AccountCreated -> R.string.notifications_type_account_created
    PasswordReset -> R.string.notifications_type_password_reset
    LoginAlert -> R.string.notifications_type_login_alert
    DocumentShared -> R.string.notifications_type_document_shared
    ReportReady -> R.string.notifications_type_report_ready
    AbsenceIntention -> R.string.notifications_type_absence_intention
    AbsenceIntentionDecision -> R.string.notifications_type_absence_intention_decision
    AbsenceUnreportedFollowup -> R.string.notifications_type_absence_unreported_followup
    SetupGuide -> R.string.notifications_type_setup_guide
    LiveClassScheduled -> R.string.notifications_type_live_class_scheduled
    LiveClassStartingSoon -> R.string.notifications_type_live_class_starting_soon
    LiveClassStarted -> R.string.notifications_type_live_class_started
    LiveClassCancelled -> R.string.notifications_type_live_class_cancelled
    LiveClassRecordingReady -> R.string.notifications_type_live_class_recording_ready
    ContentReview -> R.string.notifications_type_content_review
    ContentApproved -> R.string.notifications_type_content_approved
    ContentRejected -> R.string.notifications_type_content_rejected
}

/** `preferences-form.tsx` `CHANNEL_ICONS`. */
internal fun NotificationChannel.icon(): ImageVector = when (this) {
    NotificationChannel.InApp -> Icons.Outlined.Notifications
    NotificationChannel.Email -> Icons.Outlined.MailOutline
    NotificationChannel.Push -> Icons.Outlined.Smartphone
    NotificationChannel.Sms -> Icons.Outlined.ChatBubbleOutline
    NotificationChannel.WhatsApp -> Icons.Outlined.ModeComment
}

/** `dictionary.notifications.preferences.channels`. */
@StringRes
internal fun NotificationChannel.labelRes(): Int = when (this) {
    NotificationChannel.InApp -> R.string.notifications_channel_in_app
    NotificationChannel.Email -> R.string.notifications_channel_email
    NotificationChannel.Push -> R.string.notifications_channel_push
    NotificationChannel.Sms -> R.string.notifications_channel_sms
    NotificationChannel.WhatsApp -> R.string.notifications_channel_whatsapp
}
