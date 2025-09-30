package com.example.splashscreen

import android.os.Bundle
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity

class FaqActivity : AppCompatActivity() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var faqAdapter: FaqAdapter
    private lateinit var faqQuestions: List<String>
    private lateinit var faqAnswers: HashMap<String, List<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        expandableListView = findViewById(R.id.faqList)

        // Load FAQ data
        setupFAQData()

        faqAdapter = FaqAdapter(this, faqQuestions, faqAnswers)
        expandableListView.setAdapter(faqAdapter)
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

        faqAnswers = HashMap()
        faqAnswers[faqQuestions[0]] = listOf("New Life Baby Home is a crisis home in KwaZulu-Natal dedicated to providing abandoned babies with safety, care, and the opportunity to be placed into loving families.")
        faqAnswers[faqQuestions[1]] = listOf("We are supported by Hillside Church and our partner NPO, iKusasalethu, who help resource and sustain our work.")
        faqAnswers[faqQuestions[2]] = listOf("Our current capacity is to care for up to 6 babies at a time, ensuring personalized care and attention.")
        faqAnswers[faqQuestions[3]] = listOf("Adoptions in South Africa are facilitated through registered adoption agencies and the Department of Social Development. We work alongside them to ensure each baby is placed in a safe, permanent home.")
        faqAnswers[faqQuestions[4]] = listOf("Each baby is cared for by trained staff in a safe, loving environment until they are either reunited with family (if possible) or placed in foster care/adoption.")
        faqAnswers[faqQuestions[5]] = listOf("Yes, mothers who feel unable to care for their babies can make the painful but brave choice of adoption. We ensure the babies are placed in safe hands and given a second chance.")
        faqAnswers[faqQuestions[6]] = listOf("It costs around R85,000 per month to cover staff salaries, utilities, food, clothing, and supplies.")
        faqAnswers[faqQuestions[7]] = listOf("Donations can be made via our BackaBuddy campaign, BabyChino, either as once-off contributions or recurring support. Donations can also be made by scanning the Zapper Code or EFT")
        faqAnswers[faqQuestions[8]] = listOf("We welcome donations of baby clothes, formula, nappies, and food supplies. Volunteers are also encouraged to support our outreach programs.")
        faqAnswers[faqQuestions[9]] = listOf("Socioeconomic challenges such as poverty, trauma, rape, and fractured family structures are major contributors to child abandonment. Around 10,000 babies are abandoned annually in SA.")
        faqAnswers[faqQuestions[10]] = listOf("Unfortunately, adoption rates remain low due to social stigma and legal processes, but we believe every child deserves a family.")
        faqAnswers[faqQuestions[11]] = listOf("By supporting mothers in crisis, creating awareness of safe alternatives like adoption, and investing in programs that strengthen families and reduce poverty.")
    }
}
