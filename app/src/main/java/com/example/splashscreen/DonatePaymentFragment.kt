package com.example.splashscreen

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.*

class DonatePaymentFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var textRaised: TextView
    private lateinit var textGoal: TextView
    private lateinit var textGoalPublic: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var imageBottle: ImageView

    // Positioned to the bottle cavity
    private lateinit var milkClip: FrameLayout

    // Base fill (solid milk up to just below the surface) + thin band that hosts the ripple
    private lateinit var milkSolid: View
    private lateinit var milkSurface: FrameLayout

    // (Legacy waves, if still in XML)
    private var milkFillBack: ImageView? = null
    private var milkFillFront: ImageView? = null

    private lateinit var editButton: Button

    // Ripple renderer
    private var rippleView: RippleWaveView? = null
    private var rippleAnimator: ValueAnimator? = null

    private var totalRaised = 0.0
    private var goalAmount = 10000.0
    private var lastProgress = 0.0
    private var donationListener: ListenerRegistration? = null

    // Local cache so state sticks immediately (and offline)
    private val prefs by lazy { requireContext().getSharedPreferences("donation_progress", Context.MODE_PRIVATE) }

    // Vector
    private val VPW = 140f
    private val VPH = 280f
    private val INNER_LEFT = 36f
    private val INNER_RIGHT = 104f
    private val INNER_TOP = 84f
    private val INNER_BOTTOM = 236f

    // Visual tuning
    private val SCALE_FACTOR = 1.0f
    private val HEADROOM_DP = 6f             // normal small gap near the very top
    private val GOAL_HEADROOM_DP = 1.5f      // even smaller gap when goal reached
    private val BASELINE_DP = 8f             // always show a little milk
    private val SURFACE_HEIGHT_DP = 24f      // surface band height
    private val RIPPLE_AMPLITUDE_DP = 6f     // ripple size
    private val RIPPLE_WAVELENGTH_FACTOR = 1.10f
    private val RIPPLE_PERIOD_MS = 3000L
    private val MENISCUS_MAX_DP = 2.6f
    private val ANIM_MS = 900L
    private val CHANGE_EPS = 0.25

    //visual baseline boost
    private val BASELINE_VIRTUAL_DONATION = 1000.0      // R 1,000
    private val BASELINE_MAX_BOOST_PCT = 25.0           // don’t boost more than 25% of the height

    //fill when goal reached
    private val GOAL_OVERFILL_MULTIPLIER = 1.5f         // ~1.5x higher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_donate_payment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textRaised = view.findViewById(R.id.textRaised)
        textGoal = view.findViewById(R.id.textGoal)
        textGoalPublic = view.findViewById(R.id.textGoalPublic)
        progressBar = view.findViewById(R.id.donationProgressBar)
        imageBottle = view.findViewById(R.id.imageBottle)
        milkClip = view.findViewById(R.id.milkClip)
        milkSolid = view.findViewById(R.id.milkSolid)
        milkSurface = view.findViewById(R.id.milkSurface)
        editButton = view.findViewById(R.id.btnEditDonation)

        //milk
        milkSolid.setBackgroundColor(Color.WHITE)

        // Bottom-align both children inside milkClip
        (milkSolid.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.BOTTOM
            milkSolid.layoutParams = this
        }
        (milkSurface.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.BOTTOM
            milkSurface.layoutParams = this
        }
        milkSurface.bringToFront()

        // Hide legacy waves if present
        milkFillBack = view.findViewById(R.id.milkFillBack)
        milkFillFront = view.findViewById(R.id.milkFillFront)
        milkFillBack?.visibility = View.GONE
        milkFillFront?.visibility = View.GONE

        // Add pure white ripple view in the surface band
        rippleView = RippleWaveView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        milkSurface.addView(rippleView)

        editButton.visibility = if (SessionManager.isAdmin) View.VISIBLE else View.GONE
        if (!SessionManager.isAdmin) {
            textRaised.visibility = View.GONE
            textGoal.visibility = View.GONE
        } else {
            textRaised.visibility = View.VISIBLE
            textGoal.visibility = View.VISIBLE
        }
        textGoalPublic.visibility = View.GONE

        editButton.setOnClickListener { showEditDialog() }

        view.findViewById<ImageView>(R.id.buttonBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.button)?.setOnClickListener {
            Snackbar.make(view, "Thank you for your donation", Snackbar.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.btnZapper)?.setOnClickListener { showQrDialog(R.drawable.zapper, "Zapper QR") }

        //EFT copyable dialog
        view.findViewById<Button>(R.id.btnEFT)?.apply {
            setOnClickListener { showEftDetailsDialog() }
            setOnLongClickListener {
                copyEftDetailsToClipboard()
                Snackbar.make(view, "Copied EFT details", Snackbar.LENGTH_SHORT).show()
                true
            }
        }

        view.findViewById<Button>(R.id.btnBabychino)?.setOnClickListener { showQrDialog(R.drawable.babychino_qr, "BabyChino QR") }

        // Load cached values immediately
        loadCached()
        progressBar.progress = lastProgress.toInt()

        // Position cavity to match vector bottle
        imageBottle.doOnLayout { positionMilkClip(applyInstant = true) }
        imageBottle.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            positionMilkClip(applyInstant = true)
        }
    }

    override fun onStart() {
        super.onStart()
        donationListener = db.collection("donationProgress").document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val newRaised = snapshot.getDouble("totalRaised") ?: 0.0
                    val newGoal = snapshot.getDouble("monthlyGoal") ?: 10000.0
                    val newProgress = progressPercent(newRaised, newGoal)
                    val changed = abs(newProgress - lastProgress) >= CHANGE_EPS

                    totalRaised = newRaised
                    goalAmount = newGoal

                    cacheCurrent()
                    updateUI(animate = changed)
                }
            }
    }

    override fun onStop() {
        super.onStop()
        donationListener?.remove()
        donationListener = null
        rippleAnimator?.cancel()
        rippleAnimator = null
    }

    private fun showEditDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_donation, null)

        val inputRaised = dialogView.findViewById<EditText>(R.id.inputTotalRaised)
        val inputGoal = dialogView.findViewById<EditText>(R.id.inputGoalAmount)

        inputRaised.setText(totalRaised.toString())
        inputGoal.setText(goalAmount.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Update Donation Progress")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newRaised = inputRaised.text.toString().toDoubleOrNull() ?: totalRaised
                val newGoal = inputGoal.text.toString().toDoubleOrNull() ?: goalAmount
                saveDonationProgress(newRaised, newGoal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveDonationProgress(raised: Double, goal: Double) {
        val data = mapOf("totalRaised" to raised, "monthlyGoal" to goal)
        db.collection("donationProgress").document("current")
            .set(data)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Updated successfully", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Failed to update", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun loadCached() {
        val cachedRaised = prefs.getFloat("raised", Float.NaN)
        val cachedGoal = prefs.getFloat("goal", Float.NaN)
        if (!cachedRaised.isNaN() && !cachedGoal.isNaN() && cachedGoal > 0f) {
            totalRaised = cachedRaised.toDouble()
            goalAmount = cachedGoal.toDouble()
        }
        lastProgress = progressPercent(totalRaised, goalAmount)
    }

    private fun cacheCurrent() {
        prefs.edit()
            .putFloat("raised", totalRaised.toFloat())
            .putFloat("goal", goalAmount.toFloat())
            .apply()
    }

    private fun progressPercent(raised: Double, goal: Double): Double {
        val safeGoal = if (goal > 0.0) goal else 1.0
        return ((raised / safeGoal) * 100.0).coerceIn(0.0, 100.0)
    }

    private fun updateUI(animate: Boolean) {
        val progress = progressPercent(totalRaised, goalAmount) // TRUE progress (no visual boost)
        val reached = progress >= 100.0
        lastProgress = progress

        if (animate) {
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progress.toInt())
                .apply { duration = ANIM_MS; start() }
            animateMilk(progress) // visual mapping happens inside
        } else {
            progressBar.progress = progress.toInt()
            setMilkInstant(progress)
        }

        if (SessionManager.isAdmin) {
            textRaised.visibility = View.VISIBLE
            textGoal.visibility = View.VISIBLE
            textRaised.text = "Raised R %.2f".format(totalRaised)
            textGoal.text = if (reached) "Goal reached Thank you for your support"
            else "Goal R %.2f".format(goalAmount)
            textGoalPublic.visibility = View.GONE
        } else {
            textGoalPublic.visibility = if (reached) View.VISIBLE else View.GONE
        }
    }

    //position the milk clip to the bottle’s inner cavity
    private fun positionMilkClip(applyInstant: Boolean) {
        if (imageBottle.width == 0 || imageBottle.height == 0 || imageBottle.drawable == null) return

        val content = imageContentRect(imageBottle)
        val scaleX = content.width() / VPW
        val scaleY = content.height() / VPH

        val left = (content.left + INNER_LEFT * scaleX).toInt()
        val top = (content.top + INNER_TOP * scaleY).toInt()
        val right = (content.left + INNER_RIGHT * scaleX).toInt()
        val bottom = (content.top + INNER_BOTTOM * scaleY).toInt()

        val lp = (milkClip.layoutParams as FrameLayout.LayoutParams).apply {
            width = (right - left)
            height = (bottom - top)
            leftMargin = left
            topMargin = top
        }
        milkClip.layoutParams = lp
        milkClip.visibility = View.VISIBLE

        if (applyInstant) setMilkInstant(lastProgress)

        startRipple() // run when sizes are known
    }

    // Where the drawable is actually drawn inside the ImageView
    private fun imageContentRect(iv: ImageView): RectF {
        val d = iv.drawable!!
        val vw = (iv.width - iv.paddingLeft - iv.paddingRight).toFloat()
        val vh = (iv.height - iv.paddingTop - iv.paddingBottom).toFloat()
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        val scale = min(vw / dw, vh / dh)
        val contentW = dw * scale
        val contentH = dh * scale
        val left = iv.paddingLeft + (vw - contentW) / 2f
        val top = iv.paddingTop + (vh - contentH) / 2f
        return RectF(left, top, left + contentW, top + contentH)
    }

    /**
     * Visual mapping:
     * - Add a baseline boost equivalent to ~R1000 so 0 shows some milk.
     * - If goal reached, “overfill” visually (up to ~1.5× previous max).
     */
    private fun animateMilk(trueProgressPercent: Double) {
        val runnable = Runnable {
            val reached = trueProgressPercent >= 100.0
            val headroomPx = dpInt(if (reached) GOAL_HEADROOM_DP else HEADROOM_DP)
            val baselinePx = dpInt(BASELINE_DP)
            val surfacePxRaw = dpInt(SURFACE_HEIGHT_DP)

            val maxBase = (milkClip.height - headroomPx).coerceAtLeast(baselinePx)

            // Baseline visual boost (~R1000 as a percent of goal, capped)
            val safeGoal = if (goalAmount > 0.0) goalAmount else 1.0
            val boostPct = ((BASELINE_VIRTUAL_DONATION / safeGoal) * 100.0)
                .coerceIn(0.0, BASELINE_MAX_BOOST_PCT)

            // Apply boost only to visual mapping (not progress bar / admin numbers)
            var displayPct = (trueProgressPercent + boostPct).coerceIn(0.0, 100.0)

            // Overfill when goal is reached
            var scaled = ((displayPct / 100.0) * SCALE_FACTOR).coerceAtMost(1.0)
            if (reached) {
                scaled = min(1.0, scaled * GOAL_OVERFILL_MULTIPLIER)
            }

            val targetMilk = (baselinePx + scaled * (maxBase - baselinePx)).toInt()

            // Split into solid + surface band
            val bandHeight = min(surfacePxRaw, max(dpInt(12f), targetMilk))
            val solidHeight = (targetMilk - bandHeight).coerceAtLeast(0)

            val startSolid = (milkSolid.layoutParams.height).coerceAtLeast(0)
            val startBand = (milkSurface.layoutParams.height).coerceAtLeast(0)
            val startBottomMargin = (milkSurface.layoutParams as FrameLayout.LayoutParams).bottomMargin

            val animSolid = ValueAnimator.ofInt(startSolid, solidHeight).apply { duration = ANIM_MS }
            val animBandH = ValueAnimator.ofInt(startBand, bandHeight).apply { duration = ANIM_MS }
            val animBandBM = ValueAnimator.ofInt(startBottomMargin, solidHeight).apply { duration = ANIM_MS }

            animSolid.addUpdateListener {
                (milkSolid.layoutParams as FrameLayout.LayoutParams).apply {
                    height = it.animatedValue as Int
                    gravity = Gravity.BOTTOM
                    milkSolid.layoutParams = this
                }
            }
            val applyBand: (Int, Int) -> Unit = { h, bm ->
                (milkSurface.layoutParams as FrameLayout.LayoutParams).apply {
                    height = h
                    gravity = Gravity.BOTTOM
                    bottomMargin = bm
                    milkSurface.layoutParams = this
                }
                milkSurface.bringToFront()
                rippleView?.setBandHeight(h)
            }
            animBandH.addUpdateListener { va ->
                applyBand(va.animatedValue as Int, (milkSurface.layoutParams as FrameLayout.LayoutParams).bottomMargin)
            }
            animBandBM.addUpdateListener { va ->
                applyBand((milkSurface.layoutParams.height), va.animatedValue as Int)
            }

            animSolid.start()
            animBandH.start()
            animBandBM.start()
        }

        if (milkClip.height == 0) milkClip.post(runnable) else runnable.run()
    }

    private fun setMilkInstant(trueProgressPercent: Double) {
        val reached = trueProgressPercent >= 100.0
        val headroomPx = dpInt(if (reached) GOAL_HEADROOM_DP else HEADROOM_DP)
        val baselinePx = dpInt(BASELINE_DP)
        val surfacePxRaw = dpInt(SURFACE_HEIGHT_DP)

        val maxBase = (milkClip.height - headroomPx).coerceAtLeast(baselinePx)

        val safeGoal = if (goalAmount > 0.0) goalAmount else 1.0
        val boostPct = ((BASELINE_VIRTUAL_DONATION / safeGoal) * 100.0)
            .coerceIn(0.0, BASELINE_MAX_BOOST_PCT)
        var displayPct = (trueProgressPercent + boostPct).coerceIn(0.0, 100.0)

        var scaled = ((displayPct / 100.0) * SCALE_FACTOR).coerceAtMost(1.0)
        if (reached) {
            scaled = min(1.0, scaled * GOAL_OVERFILL_MULTIPLIER)
        }

        val targetMilk = (baselinePx + scaled * (maxBase - baselinePx)).toInt()
        val bandHeight = min(surfacePxRaw, max(dpInt(12f), targetMilk))
        val solidHeight = (targetMilk - bandHeight).coerceAtLeast(0)

        (milkSolid.layoutParams as FrameLayout.LayoutParams).apply {
            height = solidHeight
            gravity = Gravity.BOTTOM
            milkSolid.layoutParams = this
        }
        (milkSurface.layoutParams as FrameLayout.LayoutParams).apply {
            height = bandHeight
            gravity = Gravity.BOTTOM
            bottomMargin = solidHeight
            milkSurface.layoutParams = this
        }
        milkSurface.bringToFront()
        rippleView?.setBandHeight(bandHeight)
    }

    /** Start an infinite, smooth ripple EXACTLY at the milk surface (pure white). */
    private fun startRipple() {
        if (milkSurface.width == 0) {
            milkSurface.post { startRipple() }
            return
        }
        rippleAnimator?.cancel()

        rippleView?.let { rv ->
            rv.setBandWidth(milkSurface.width)
            rv.setWaveStyle(
                amplitudePx = dp(RIPPLE_AMPLITUDE_DP),
                wavelengthPx = max(140f, milkSurface.width * RIPPLE_WAVELENGTH_FACTOR),
                crestSoftness = 0.25f
            )
        }

        rippleAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
            duration = RIPPLE_PERIOD_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = null // linear for seamless loop
            addUpdateListener { va ->
                rippleView?.setPhase(va.animatedValue as Float)
            }
            start()
        }
    }

    private fun showQrDialog(drawableRes: Int, title: String) {
        val b = AlertDialog.Builder(requireContext())
        b.setTitle(title)
        val img = ImageView(requireContext()).apply {
            setImageResource(drawableRes)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(32, 32, 32, 16)
        }
        b.setView(img)
        b.setPositiveButton("Close") { d, _ -> d.dismiss() }
        b.show()
    }

    //EFT copyable dialog + helper

    private fun showEftDetailsDialog() {
        val details = """
            Ikusasalethu Baby Home
            Standard Bank
            Hillcrest
            Account number 052660093
            Branch code 045726
            Reference Your Name
        """.trimIndent()

        // Selectable text so users can copy specific lines too
        val tv = TextView(requireContext()).apply {
            text = details
            setTextIsSelectable(true)
            setPadding(dpInt(16f), dpInt(12f), dpInt(16f), dpInt(8f))
            textSize = 16f
        }

        AlertDialog.Builder(requireContext())
            .setTitle("EFT details")
            .setView(tv)
            .setPositiveButton("Copy") { d, _ ->
                copyToClipboard("EFT details", details)
                Snackbar.make(requireView(), "EFT details copied", Snackbar.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun copyEftDetailsToClipboard() {
        val details = """
            Ikusasalethu Baby Home
            Standard Bank
            Hillcrest
            Account number 052660093
            Branch code 045726
            Reference Your Name
        """.trimIndent()
        copyToClipboard("EFT details", details)
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }


    //ripple renderer
    private class RippleWaveView(context: Context) : View(context) {
        private val milkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        private val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 1.4f * resources.displayMetrics.density
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()

        private var phase = 0f
        private var amplitude = 6f
        private var wavelength = 220f
        private var softness = 0.25f
        private var bandHeight = 0
        private var bandWidth = 0

        fun setBandHeight(h: Int) { bandHeight = h; invalidate() }
        fun setBandWidth(w: Int) { bandWidth = w; invalidate() }

        fun setWaveStyle(amplitudePx: Float, wavelengthPx: Float, crestSoftness: Float) {
            amplitude = amplitudePx
            wavelength = wavelengthPx
            softness = crestSoftness.coerceIn(0.05f, 0.6f)
            invalidate()
        }

        fun setPhase(p: Float) { phase = p; invalidate() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (bandHeight <= 0) return

            val w = width.toFloat()
            val h = height.toFloat()

            val a = min(amplitude, h * 0.9f)
            val meniscusAmp = min(h * 0.25f, 3f * resources.displayMetrics.density)
            val crestMargin = 1f * resources.displayMetrics.density
            val baseY = max(crestPaint.strokeWidth + a * 1.3f + meniscusAmp + crestMargin, h * 0.25f)

            val k = (2f * Math.PI / max(1f, wavelength)).toFloat()

            fun meniscus(x: Float): Float {
                if (w == 0f) return 0f
                val t = x / w // 0..1; small bow up at edges
                return meniscusAmp * ((cos((Math.PI * 2.0 * t).toFloat()) + 1f) * 0.5f)
            }

            fun waveY(x: Float): Float {
                val s1 = sin(k * x + phase)
                val s2 = 0.18f * sin(2f * k * x + phase)   // gentle rounding
                val s = (s1 + s2) / 1.18f
                return a * s
            }

            val samples = max(48, (w / 5f).roundToInt())
            path.reset()

            var x = 0f
            var y = baseY - waveY(0f) - meniscus(0f)
            path.moveTo(0f, y)
            for (i in 1..samples) {
                x = w * i / samples
                y = baseY - waveY(x) - meniscus(x)
                path.lineTo(x, y)
            }

            // Close to bottom & fill (pure white)
            path.lineTo(w, h)
            path.lineTo(0f, h)
            path.close()
            canvas.drawPath(path, milkPaint)

            // Crest outline
            path.reset()
            x = 0f
            y = baseY - waveY(0f) - meniscus(0f)
            path.moveTo(0f, y)
            for (i in 1..samples) {
                x = w * i / samples
                y = baseY - waveY(x) - meniscus(x)
                path.lineTo(x, y)
            }
            canvas.drawPath(path, crestPaint)
        }
    }

    // ---- dp helpers ----
    private fun dp(valueDp: Float): Float =
        valueDp * resources.displayMetrics.density

    private fun dpInt(valueDp: Float): Int =
        (dp(valueDp) + 0.5f).toInt()
}
