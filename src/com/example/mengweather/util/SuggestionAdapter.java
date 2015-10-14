package com.example.mengweather.util;

import java.util.List;

import com.example.mengweather.R;
import com.example.mengweather.model.Suggestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SuggestionAdapter extends ArrayAdapter<Suggestion> {
	
	private int resourceID;
	
	public SuggestionAdapter(Context context, int resource,
			List<Suggestion> objects) {
		super(context, resource, objects);
		resourceID=resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Suggestion suggestion=getItem(position);
		View view=LayoutInflater.from(getContext()).inflate(resourceID, null);
		TextView suggestion_title=(TextView) view.findViewById(R.id.suggestion_title);
		TextView index=(TextView) view.findViewById(R.id.index);
		TextView detail=(TextView) view.findViewById(R.id.detail);
		suggestion_title.setText(suggestion.suggestion_title);
		index.setText(suggestion.index);
		detail.setText(suggestion.detail);
		return view;
	}
	
}
