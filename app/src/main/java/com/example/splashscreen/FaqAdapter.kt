package com.example.splashscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class FaqAdapter(
    private val context: Context,
    private val questions: List<String>,
    private val answers: HashMap<String, List<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = questions.size

    override fun getChildrenCount(groupPosition: Int): Int =
        answers[questions[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = questions[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        answers[questions[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val question = getGroup(groupPosition) as String
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = question
        textView.textSize = 16f
        textView.setPadding(40, 20, 20, 20)
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val answer = getChild(groupPosition, childPosition) as String
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = answer
        textView.textSize = 14f
        textView.setPadding(60, 15, 15, 15)
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
