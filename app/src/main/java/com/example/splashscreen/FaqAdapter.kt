package com.example.splashscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView

class FaqAdapter(
    private val context: Context,
    private val questions: List<String>,
    private val answers: HashMap<String, List<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = questions.size
    override fun getChildrenCount(groupPosition: Int): Int = answers[questions[groupPosition]]?.size ?: 0
    override fun getGroup(groupPosition: Int): Any = questions[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        answers[questions[groupPosition]]?.get(childPosition) ?: ""
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun hasStableIds(): Boolean = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_faq_group, parent, false)
        val tvNumber = view.findViewById<TextView>(R.id.tvNumber)
        val tvQuestion = view.findViewById<TextView>(R.id.tvQuestion)
        val ivChevron = view.findViewById<ImageView>(R.id.ivChevron)

        tvNumber.text = "${groupPosition + 1}"
        tvQuestion.text = getGroup(groupPosition) as String
        ivChevron.rotation = if (isExpanded) 180f else 0f

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_faq_child, parent, false)
        val tvAnswer = view.findViewById<TextView>(R.id.tvAnswer)
        tvAnswer.text = getChild(groupPosition, childPosition) as String
        return view
    }
}
