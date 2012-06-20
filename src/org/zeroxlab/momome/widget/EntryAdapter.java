/*
 * Authored By Julian Chu <walkingice@0xlab.org>
 *
 * Copyright (c) 2012 0xlab.org - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zeroxlab.momome.widget;

import org.zeroxlab.momome.R;
import org.zeroxlab.momome.Momo;
import org.zeroxlab.momome.MomoApp;
import org.zeroxlab.momome.MomoModel;
import org.zeroxlab.momome.data.Item;
import org.zeroxlab.momome.data.Item.ItemEntry;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class EntryAdapter extends BaseAdapter implements Momo {

    protected Context         mContext;
    protected LayoutInflater  mInflater;
    protected Item            mItem;
    protected boolean         mIsEditing = false;

    public EntryAdapter(Context context, Item item) {
        super();
        mContext  = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItem     = item;
    }

    @Override
    public int getCount() {
        return mItem.getEntryCount();
    }

    @Override
    public Object getItem(int pos) {
        return mItem.getEntry(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.entry_editable, null);
         }

         ItemEntry entry = (ItemEntry) getItem(pos);
         updateOneRow(entry, convertView);

         return convertView;
    }

    public void setEditing(boolean editing) {
        mIsEditing = editing;
    }

    private void updateOneRow(ItemEntry entry, View view) {
        TextView viewKey   = (TextView) view.findViewById(R.id.entry_key);
        TextView viewValue = (TextView) view.findViewById(R.id.entry_value);
        View btnName   = view.findViewById(R.id.entry_btn_name);
        View btnValue  = view.findViewById(R.id.entry_btn_value);

        btnName.setTag(viewKey);
        viewKey.setTag(entry);
        btnValue.setTag(viewValue);
        viewValue.setTag(entry);

        viewKey.setText(entry.getName());
        viewValue.setText(entry.getContent());
        if (mIsEditing) {
            btnName.setVisibility(View.VISIBLE);
            btnValue.setVisibility(View.VISIBLE);
        } else {
            btnName.setVisibility(View.GONE);
            btnValue.setVisibility(View.GONE);
        }
    }
}