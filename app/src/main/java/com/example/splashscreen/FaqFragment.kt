package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment

class FaqFragment : Fragment() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var faqAdapter: FaqAdapter
    private lateinit var faqQuestions: List<String>
    private lateinit var faqAnswers: HashMap<String, List<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_faq, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back should ALWAYS go home
        view.findViewById<View>(R.id.iv_back).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        expandableListView = view.findViewById(R.id.faqList)
        expandableListView.setGroupIndicator(null)

        setupFAQData()
        faqAdapter = FaqAdapter(requireContext(), faqQuestions, faqAnswers)
        expandableListView.setAdapter(faqAdapter)

        expandableListView.setOnGroupExpandListener { expanded ->
            for (i in 0 until faqAdapter.groupCount) if (i != expanded) expandableListView.collapseGroup(i)
        }
    }

    private fun setupFAQData() {
        faqQuestions = listOf(
            "What is New Life Baby Home?",
            "Who supports New Life Baby Home?",
            "How many babies can the home care for at a time?",
            "How does the adoption process work?",
            "What happens to a baby once they arrive at New Life Baby Home?",
            "Can birth mothers safely give up their child?",
            "How much does it cost to run the home each month?",
            "How can I donate?",
            "Besides money, what else can I contribute?",
            "Why are so many babies abandoned in South Africa?",
            "Is adoption common in South Africa?",
            "How can communities help prevent child abandonment?"
        )

        faqAnswers = hashMapOf(
            faqQuestions[0] to listOf("New Life Baby Home is a crisis home in KwaZulu-Natal dedicated to providing abandoned babies with safety, care, and the opportunity to be placed into loving families."),
            faqQuestions[1] to listOf("We are supported by Hillside Church and our partner NPO, iKusasalethu, who help resource and sustain our work."),
            faqQuestions[2] to listOf("Our current capacity is to care for up to 6 babies at a time, ensuring personalized care and attention."),
            faqQuestions[3] to listOf("Adoptions in South Africa are facilitated through registered adoption agencies and the Department of Social Development. We work alongside them to ensure each baby is placed in a safe, permanent home."),
            faqQuestions[4] to listOf("Each baby is cared for by trained staff in a safe, loving environment until they are either reunited with family (if possible) or placed in foster care/adoption."),
            faqQuestions[5] to listOf("Yes, mothers who feel unable to care for their babies can make the painful but brave choice of adoption. We ensure the babies are placed in safe hands and given a second chance."),
            faqQuestions[6] to listOf("It costs around R85,000 per month to cover staff salaries, utilities, food, clothing, and supplies."),
            faqQuestions[7] to listOf("Donations can be made via our BackaBuddy campaign or BabyChino—either once-off or recurring. Donations can also be made via Zapper or EFT."),
            faqQuestions[8] to listOf("We welcome donations of baby clothes, formula, nappies, and food supplies. Volunteers are also encouraged to support our outreach programs."),
            faqQuestions[9] to listOf("Socioeconomic challenges such as poverty, trauma, rape, and fractured family structures are major contributors to child abandonment. Around 10,000 babies are abandoned annually in SA."),
            faqQuestions[10] to listOf("Unfortunately, adoption rates remain low due to social stigma and legal processes, but we believe every child deserves a family."),
            faqQuestions[11] to listOf("By supporting mothers in crisis, creating awareness of safe alternatives like adoption, and investing in programs that strengthen families and reduce poverty.")
        )
    }
}
