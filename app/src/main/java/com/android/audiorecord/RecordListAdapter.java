package com.android.audiorecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 * @author tianchi.deng
 * @description:
 * @date :5/10/21 5:24 PM
 */
public class RecordListAdapter extends BaseAdapter {
    private Context mContext;
    private List<File> files;
    public RecordListAdapter(Context context, List<File> list) {
        mContext = context;
        files = list;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item, null);
            viewHolder.fileName = convertView.findViewById(R.id.file_name);
            viewHolder.fileSize = convertView.findViewById(R.id.file_size);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fileName.setText(files.get(position).getName());
        viewHolder.fileSize.setText(FormetFileSize(files.get(position).length()));
        return convertView;
    }

    public String FormetFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    class ViewHolder {
        TextView fileName;
        TextView fileSize;
    }
}
