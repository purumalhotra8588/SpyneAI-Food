import com.google.gson.annotations.SerializedName

data class InvitationEmailBody(
    @SerializedName("auth_key")
    val authkey: String,
    @SerializedName("mailList")
    val mailList: ArrayList<String>,
)