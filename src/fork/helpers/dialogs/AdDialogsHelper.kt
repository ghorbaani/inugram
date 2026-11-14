package desu.inugram.helpers.dialogs

import androidx.core.content.edit
import desu.inugram.InuConfig

/**
 * Device-local list of dialogs the user explicitly marks as advertising-only.
 * Hiding is client-side; the underlying Telegram dialogs are never deleted.
 */
object AdDialogsHelper {
    private fun key(account: Int) = "ad_dialogs_hidden:$account"

    @JvmStatic
    fun isHidden(account: Int, dialogId: Long): Boolean {
        return InuConfig.prefs.getStringSet(key(account), emptySet())
            ?.contains(dialogId.toString()) == true
    }

    @JvmStatic
    fun getHidden(account: Int): Set<Long> {
        return InuConfig.prefs.getStringSet(key(account), emptySet())
            ?.mapNotNullTo(HashSet(), String::toLongOrNull) ?: emptySet()
    }

    @JvmStatic
    fun setHidden(account: Int, dialogIds: Collection<Long>) {
        InuConfig.prefs.edit {
            putStringSet(key(account), dialogIds.mapTo(HashSet(), Long::toString))
        }
    }
}
