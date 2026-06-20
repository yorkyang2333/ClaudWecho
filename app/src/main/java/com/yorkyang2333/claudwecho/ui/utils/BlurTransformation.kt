package com.yorkyang2333.claudwecho.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import coil.size.Size
import coil.transform.Transformation

class BlurTransformation(
    private val context: Context,
    private val radius: Float = 25f
) : Transformation {

    override val cacheKey: String = "blur-$radius"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val output = Bitmap.createBitmap(input.width, input.height, input.config ?: Bitmap.Config.ARGB_8888)
        
        var rs: RenderScript? = null
        try {
            rs = RenderScript.create(context)
            val allocationIn = Allocation.createFromBitmap(rs, input)
            val allocationOut = Allocation.createFromBitmap(rs, output)
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            script.setRadius(radius.coerceIn(0f, 25f))
            script.setInput(allocationIn)
            script.forEach(allocationOut)
            allocationOut.copyTo(output)
        } finally {
            rs?.destroy()
        }
        
        return output
    }
}
