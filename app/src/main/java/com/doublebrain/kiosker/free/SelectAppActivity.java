package com.doublebrain.kiosker.free;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SelectAppActivity extends AppCompatActivity {
    private ListView list;
    private View progressView;
    ArrayList<AppDataModel> res;
    AppListAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_app);
        list = (ListView) findViewById(R.id.appList);
        progressView = findViewById(R.id.getting_apps_progress);

        res = new ArrayList<AppDataModel>();

        _adapter = new AppListAdapter(getApplicationContext(), res);
        list.setAdapter(_adapter);

        new AsyncTask<Void,Void,Void>(){
            ArrayList<AppDataModel> res_temp;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressView.setVisibility(View.VISIBLE);
                res_temp = new ArrayList<AppDataModel>();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                final List<PackageInfo> _myapps = getPackageManager().getInstalledPackages(0);

                for (int i = 0; i < _myapps.size(); i++) {
                    PackageInfo p = _myapps.get(i);
                    //if (p.applicationInfo.packageName.equals("com.e1c.mobile")) continue;

                    if (getPackageManager().getLaunchIntentForPackage(p.applicationInfo.packageName)==null) continue;

                    AppDataModel _model  = new AppDataModel();
                    _model.appName=p.applicationInfo.loadLabel(getPackageManager()).toString();
                    _model.packageName=p.applicationInfo.packageName;
                    _model.icon = p.applicationInfo.loadIcon(getPackageManager());

                    res_temp.add(_model);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                res.clear();
                res.addAll(res_temp);
                _adapter.notifyDataSetChanged();
                list.invalidateViews();
                progressView.setVisibility(View.GONE);
            }
        }.execute();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final AppDataModel dataModel = res.get(i);
                Intent intent = new Intent();
                intent.putExtra("package",dataModel.packageName);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }

    private class AppDataModel {
        public String appName = "";
        public String packageName = "";
        public Drawable icon;

      }

    private class AppListAdapter extends BaseAdapter {
        Context _ctx;
        LayoutInflater inflater;
        public ArrayList<AppDataModel> data;

        public AppListAdapter(Context c, ArrayList<AppDataModel> _arraylist) {
            this._ctx = c;
            this.data = _arraylist;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            inflater = (LayoutInflater) _ctx.getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.select_app_inflator, parent, false);
            //final CheckBox checks = (CheckBox) itemView.findViewById(R.id.b);
            final TextView _setappname = (TextView) itemView.findViewById(R.id.appNameTextView);
            //checks.setChecked(data.get(position).isSelected());
            final ImageView _appImage = (ImageView) itemView.findViewById(R.id.appImageView);

            AppDataModel obj = data.get(position);
            _appImage.setImageDrawable(obj.icon);
            _setappname.setText(obj.appName);

            return itemView;
        }
    }
}
