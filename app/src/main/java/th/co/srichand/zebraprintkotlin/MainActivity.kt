package th.co.srichand.zebraprintkotlin

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.graphics.pdf.PdfRenderer
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb
import com.zebra.sdk.printer.discovery.DiscoveryHandler
import com.zebra.sdk.printer.discovery.UsbDiscoverer
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private val filter = IntentFilter(ACTION_USB_PERMISSION)
    private var mPermissionIntent: PendingIntent? = null
    private var hasPermissionToCommunicate = false
    private var mUsbManager: UsbManager? = null
    private var buttonRequestPermission: Button? = null
    private var buttonGetPDF : Button? = null
    private var buttonPrint: Button? = null
    private var buttonGetImage : Button? = null
    private var discoveredPrinterUsb: DiscoveredPrinterUsb? = null
    private var filePath: String? = null
    private var fileWidth : Int? = null
    private var fileInputStream : InputStream? =null
    private var fileLength : Int? = null
    private var pdfStatus : TextView? = null
    private var zplcode : String? = null
    private val sentZPl: MutableList<String> = ArrayList()
    private val PDF_PICK_CODE : Int = 1002
    private val IMAGE_PICK_CODE : Int = 1000
    private val READ_PERMISSION_CODE: Int = 1001

    private val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            hasPermissionToCommunicate = true
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mUsbManager = getSystemService(USB_SERVICE) as UsbManager
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        buttonRequestPermission = findViewById<View>(R.id.checkPermission) as Button
        buttonPrint = findViewById<View>(R.id.print) as Button
        buttonGetPDF = findViewById<View>(R.id.pickPDF) as Button
        buttonGetImage = findViewById<View>(R.id.getImage) as Button




        buttonRequestPermission!!.setOnClickListener {
//            Toast.makeText(applicationContext, "Checking", Toast.LENGTH_LONG).show()
//            Thread {
//                val handler: UsbDiscoveryHandler = UsbDiscoveryHandler()
//                UsbDiscoverer.findPrinters(applicationContext, handler)
//                try {
//                    while (!handler.discoveryComplete) {
//                        Thread.sleep(100)
//                    }
//                    if (handler.printers != null && handler.printers!!.size > 0) {
//                        discoveredPrinterUsb = handler.printers!![0]
//                        if (!mUsbManager!!.hasPermission(discoveredPrinterUsb!!.device)) {
//                            mUsbManager!!.requestPermission(
//                                    discoveredPrinterUsb!!.device,
//                                    mPermissionIntent
//                            )
//                        } else {
//                            hasPermissionToCommunicate = true
//                        }
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(
//                            applicationContext, e.message + e.localizedMessage, Toast.LENGTH_LONG
//                    ).show()
//                }
//            }.start()

            val mDialogView = LayoutInflater.from(this).inflate(R.layout.discovery_dialog, null)

            val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)
                    .setTitle("Login Form")
        }


        buttonGetPDF!!.setOnClickListener{
            getPDF()
        }

        buttonGetImage!!.setOnClickListener{
            getImage()
        }



        buttonPrint!!.setOnClickListener {
            if (hasPermissionToCommunicate) {
                var connection: Connection? = null
                Toast.makeText(applicationContext, "AT comm", Toast.LENGTH_LONG).show()
                try {
                    connection = discoveredPrinterUsb!!.connection
                        connection.open()
                        for (x in sentZPl.indices){
                            connection.write(sentZPl!![x].toByteArray())
                            Toast.makeText(applicationContext, "Pringt pages $x", Toast.LENGTH_LONG).show()
                        }

                } catch (e: ConnectionException) {
                    Toast.makeText(
                            applicationContext,
                            e.message + e.localizedMessage,
                            Toast.LENGTH_LONG
                    ).show()
                } catch (e: ZebraPrinterLanguageUnknownException) {
                    Toast.makeText(applicationContext,
                            e.message + e.localizedMessage,
                            Toast.LENGTH_LONG
                    ).show()
                } finally {
                    if (connection != null) {
                        try {
                            connection.close()
                        } catch (e: ConnectionException) {
                            e.printStackTrace()
                            Toast.makeText(
                                    applicationContext,
                                    e.message + e.localizedMessage,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                        applicationContext,
                        "No permission to communicate",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onPause() {
        unregisterReceiver(mUsbReceiver)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mUsbReceiver, filter)
    }

    internal inner class UsbDiscoveryHandler : DiscoveryHandler {
        var printers: MutableList<DiscoveredPrinterUsb>? = LinkedList()
        var discoveryComplete = false
        override fun foundPrinter(printer: DiscoveredPrinter) {
            printers!!.add(printer as DiscoveredPrinterUsb)
        }

        override fun discoveryFinished() {
            discoveryComplete = true
        }

        override fun discoveryError(message: String) {
            discoveryComplete = true
        }

    }

    private fun getPDF(){
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            // show pop up
            requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_PERMISSION_CODE
            )
        }
        else{
            getListPDF()
        }
    }

    private fun  getImage(){
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            // show pop up
            requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_PERMISSION_CODE
            )
        }
        else{
            getListImages()
        }
    }


    private fun getListImages(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT;
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }



    private fun getListPDF(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT;
        startActivityForResult(intent, PDF_PICK_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == PDF_PICK_CODE){
            fileInputStream = data?.data?.let { contentResolver.openInputStream(it) }
            pdfStatus = findViewById(R.id.outputPDF)
            val fileUri = data!!.data
            val fileName: String? = fileUri?.let { getPDFName(it) }
            pdfStatus?.text = fileName
             filePath = getPDFPath(fileUri!!)
             fileWidth = getPageWidth(fileUri)
            pdftoBitmapConverter(fileInputStream)

        }
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val stream = data?.data?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(stream)
            val test =  getZplCode(bitmap, true)
            zplcode = test
            val printerView = findViewById<View>(R.id.printer) as TextView
            printerView.text = test
        }
    }

    private fun pdftoBitmapConverter(fileInputStream: InputStream?) {
        val pd: PDDocument = PDDocument.load(fileInputStream)
        println("@pdfPAGE" + pd.numberOfPages)
        fileLength = pd.numberOfPages
        for(x in 0 until fileLength!!){
            val pr = PDFRenderer(pd)
            val bitmap = pr.renderImageWithDPI(x, 203F, Bitmap.Config.RGB_565)
            val image = getZplCode(bitmap, true)
            sentZPl.add(image!!)
        }
    }


    private fun getZplCode(bitmap: Bitmap, addHeaderFooter: Boolean): String? {
        val zp = ZPLconverter()
        zp.setCompressHex(true)
        zp.setBlacknessLimitPercentage(50)
        val grayBitmap: Bitmap = toGrayScale(bitmap)
        return zp.convertFromImage(grayBitmap, addHeaderFooter)
    }

    private fun toGrayScale(bmpOriginal: Bitmap): Bitmap {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val grayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(grayScale!!)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return grayScale
    }



    // Uses the Uri to obtain the name of the pdf.
    private fun getPDFName(fileUri: Uri): String? {
        val fileString = fileUri.toString()
        val myFile = File(fileString)
        var fileName: String? = null
        if (fileString.startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(fileUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        } else if (fileString.startsWith("file://")) {
            fileName = myFile.name
        }
        return fileName
    }


    // Uses the Uri to obtain the path to the file.
    private fun getPDFPath(fileUri: Uri): String? {
        var fileUri = fileUri
        val selection: String? = null
        val selectionArgs: Array<String>? = null
        val id = DocumentsContract.getDocumentId(fileUri)
        try {
            if (id.length < 15) {
                fileUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                )
            } else if (id.substring(0, 7) == "primary") {
                val endPath = id.substring(8)
                return "/sdcard/$endPath"
            } else if (id.substring(0, 1) != "/") {
                var pathStarted = false
                var path = "/sdcard/"
                for (c in id.toCharArray()) {
                    if (pathStarted) {
                        path += c
                    }
                    if (c == ':') {
                        pathStarted = true
                    }
                }
                return path
            } else {
                return id
            }
        } catch (e: NumberFormatException) {
           println("@getPDFPath $e")
        }
        if ("content".equals(fileUri.scheme, ignoreCase = true)) {
            val projection = arrayOf(
                    MediaStore.Files.FileColumns.DATA
            )
            var cursor: Cursor? = null
            try {
                cursor = contentResolver
                    .query(fileUri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: java.lang.Exception) {
            }
        } else if ("file".equals(fileUri.scheme, ignoreCase = true)) {
            return fileUri.path
        }
        return null
    }


    @Throws(IOException::class)
    private fun getPageWidth(fileUri: Uri): Int? {
        val pfdPdf = contentResolver.openFileDescriptor(
                fileUri, "r"
        )
        val pdf = PdfRenderer(pfdPdf!!)
        val page = pdf.openPage(0)
        val pixWidth = page.width
        return pixWidth / 72
    }


    companion object {
        private const val DIALOG_PDF_PICK_TAG = "pdf_pick"
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}
