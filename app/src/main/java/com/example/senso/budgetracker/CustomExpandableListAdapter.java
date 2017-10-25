package com.example.senso.budgetracker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by senso on 03/09/17.
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;

    public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                       HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String expandedListText = (String) getChild(listPosition, expandedListPosition);

        String[] NameandContent = expandedListText.split(":");
        String name = NameandContent[0];
        String almostContent[] = NameandContent[1].split(",");
        String date = almostContent[3];
        String content;
        if (almostContent[2].equals(" ")) { //no descriptioin

            content = "Costo: "+almostContent[0]+", Categoria: "+almostContent[1];
        } else {

            content = "Costo: "+almostContent[0]+", Categoria: "+almostContent[1]+", Descrizione: "+almostContent[2];
        }



        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }

        TextView expandedListTextViewTitle = (TextView) convertView
                .findViewById(R.id.expense_name);
        TextView expandedListTextViewContent= (TextView) convertView
                .findViewById(R.id.expense_content);
        TextView expandedListTextViewDate = (TextView) convertView
                .findViewById(R.id.expense_date);

        ImageView expandedListImageView = (ImageView) convertView
                .findViewById(R.id.expense_photo);

        if (listPosition == 0) {
            expandedListImageView.setImageResource(R.drawable.planned_expense_image);
        } else if (listPosition == 1) {
            expandedListImageView.setImageResource(R.drawable.periodic_expense_image);
        } else {
            expandedListImageView.setImageResource(R.drawable.normal_expense_image);
        }
        expandedListTextViewTitle.setText(name);
        expandedListTextViewDate.setText(date);

        while(content.length() > 55) {

            content = content.substring(0, content.length() - 4);
            content = content+"..";
        }
        expandedListTextViewContent.setText(content);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }


    private boolean isViewOverlapping(View firstView, View secondView) {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        firstView.getLocationOnScreen(firstPosition);
        secondView.getLocationOnScreen(secondPosition);

        int r = firstView.getMeasuredWidth() + firstPosition[0];
        int l = secondPosition[0];
        return r >= l && (r != 0 && l != 0);
    }
}

