package com.kasukusakura.danmu

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.absoluteValue


// @formatter:off
/** Imposes no restrictions on where the window can be displayed.  */
const val WDA_NONE =                0x00000000

/** The window content is displayed only on a monitor. Everywhere else, the window appears with no content.  */
const val WDA_MONITOR =             0x00000001

/**
 * The window is displayed only on a monitor. Everywhere else, the window does not appear at all.
 * One use for this affinity is for windows that show video recording controls, so that the controls are not included in the capture.
 *
 *
 * Introduced in Windows 10 Version 2004. See remarks about compatibility regarding previous versions of Windows.
 */
const val WDA_EXCLUDEFROMCAPTURE = 0x00000011

// @formatter:on

interface User32Ext : User32 {


    fun GetWindowDisplayAffinity(hwnd: HWND?, dptr: IntByReference?): Boolean
    fun GetWindowDisplayAffinity(hwnd: HWND?, dptr: Pointer): Boolean

    fun SetWindowDisplayAffinity(hwnd: HWND?, dwx: DWORD?): Boolean
}

val usr32ext = Native.load("user32", User32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)

val usr32 = User32.INSTANCE
val mem0 = Memory(2048)
val buf = CharArray(2048)
val rand = java.util.Random()

fun main() {
    //net.java.dev.jna:jna:5.12.1
    val windows = JFrame("Hider!")
    val pane = JPanel()
    windows.add(pane)
    pane.layout = MigLayout("", "[fill, grow]", "")
    windows.setLocationRelativeTo(null)
    windows.setSize(500, 400)

    refresh(pane)

    windows.isVisible = true
    windows.pack()

    scopex.launch {
        while (isActive) {
            refresh(pane)
            delay(1000)
        }
    }
}

fun refresh(pane: JPanel) {

    pane.removeAll()
    User32.INSTANCE.EnumWindows({ hwnd, ptr ->
        if (!usr32.IsWindowVisible(hwnd)) return@EnumWindows true
        val lnx = usr32.GetWindowText(hwnd, buf, 2048)
        if (lnx == 0) return@EnumWindows true
        //println("!" + String(buf, 0, lnx))

        val jlabel = JLabel(String(buf, 0, lnx))

        if (usr32ext.GetWindowDisplayAffinity(hwnd, mem0)) {
            if (mem0.getInt(0) == WDA_EXCLUDEFROMCAPTURE) {
                jlabel.foreground = Color.RED
            }
        }

        val hwndx = Pointer.nativeValue(hwnd.pointer)
        jlabel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                val hwx = HWND(Pointer.createConstant(hwndx))
                val offx = mem0.share((rand.nextInt().absoluteValue * Int.SIZE_BYTES.toLong()) % (2048))

                if (usr32ext.GetWindowDisplayAffinity(hwx, offx)) {
                    val lnx2 = usr32.GetWindowText(hwnd, buf, 2048)
                    val strx = String(buf, 0, lnx2)

                    val rspx = if (offx.getInt(0) == WDA_EXCLUDEFROMCAPTURE) {
                        println("$strx: AAA")
                        usr32ext.SetWindowDisplayAffinity(hwx, DWORD(WDA_NONE.toLong()))
                    } else {
                        println("$strx: BBB")
                        usr32ext.SetWindowDisplayAffinity(hwx, DWORD(WDA_EXCLUDEFROMCAPTURE.toLong()))
                    }
                    println(rspx)
                }
            }
        })

        pane.add(jlabel, "wrap")
        true
    }, null)
    pane.revalidate()

}