package com.example.ig

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.example.ig.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


private const val SHARE_IMAGE_TYPE = "image/*"
private const val SHARE_VIDEO_TYPE = "video/mp4"

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        fakeDownload()?.let { uri ->
            binding.shareButton.setOnClickListener {
                startShareImage(uri, this, "Example", null)
            }
        }
    }

    private fun startShareImage(
        uri: Uri,
        activity: Activity,
        title: String? = null,
        shareText: String? = null
    ) {
        val intent = Intent.createChooser(
            ShareCompat.IntentBuilder.from(activity)
                .intent
                .setAction(Intent.ACTION_SEND)
                .setType(SHARE_IMAGE_TYPE)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .apply {
                    if (title != null) {
                        putExtra(Intent.EXTRA_TITLE, title)
                        putExtra(Intent.EXTRA_SUBJECT, title)
                    }

                    if (shareText != null) {
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                },
            null
        // Adding the next line doesn't create a sticker for IG
        ).putExtra(Intent.EXTRA_INITIAL_INTENTS, createInstagramStickerIntent(uri, this))


        /*
            THIS CODE CREATES A STICKER SUCCESSFULLY

        val intent = Intent.createChooser(
            createInstagramStickerIntent(uri, this), null
        )*/

        activity.startActivity(intent)
    }

    private fun createInstagramVideoIntent(videoUri: Uri, activity: Activity) =
        Intent(Intent.ACTION_SEND).apply {

            type = SHARE_VIDEO_TYPE
            putExtra(Intent.EXTRA_STREAM, videoUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        }

    private fun createInstagramStickerIntent(stickerAssetUri: Uri, activity: Activity) =
        Intent("com.instagram.share.ADD_TO_STORY").apply {

            val sourceApplication = activity.packageName

            putExtra("source_application", sourceApplication)

            type = SHARE_IMAGE_TYPE
            putExtra("interactive_asset_uri", stickerAssetUri)
            putExtra("top_background_color", "#33FF33")
            putExtra("bottom_background_color", "#FF00FF")

            activity.grantUriPermission(
                "com.instagram.android", stickerAssetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    private fun createInstagramStickerBackgroundIntent(
        stickerAssetUri: Uri,
        videoUri: Uri,
        activity: Activity
    ) =
        Intent("com.instagram.share.ADD_TO_STORY").apply {


            putExtra("source_application", activity.packageName);

            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            setDataAndType(videoUri, "video/mp4");
            putExtra("interactive_asset_uri", stickerAssetUri);

            activity.grantUriPermission(
                "com.instagram.android", stickerAssetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    private fun fakeDownload(): Uri? {
        try {
            val file =
                File(getExternalFilesDir(null), System.currentTimeMillis().toString() + ".png")
            file.createNewFile()
            val b = BitmapFactory.decodeResource(
                resources,
                R.drawable.ig
            )
            FileOutputStream(file).use { out ->
                b.compress(Bitmap.CompressFormat.PNG, 100, out)
            }


            return FileProvider.getUriForFile(
                this,
                applicationContext.packageName.toString() + ".provider",
                file
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return null
        }
    }
}
