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

package org.zeroxlab.momome.impl;

import org.zeroxlab.momome.R;
import org.zeroxlab.momome.data.Item;;
import org.zeroxlab.momome.Momo;
import org.zeroxlab.momome.MomoApp;
import org.zeroxlab.momome.MomoModel;
import org.zeroxlab.momome.widget.BasicInputDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PrefMain extends PreferenceActivity implements Momo, MomoModel.StatusListener {

    OnPreferenceClickListener mDataActionListener;
    MomoModel mModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = MomoApp.getModel();

        addPreferencesFromResource(R.xml.pref_main);
        Preference exportPref = findPreference(KEY_EXPORT_DATA);
        Preference importPref = findPreference(KEY_IMPORT_DATA);
        Preference deletePref = findPreference(KEY_DELETE_DATA);
        Preference changePref = findPreference(KEY_CHANGE_PASSWORD);
        Preference sendPref   = findPreference(KEY_SEND_DATA);

        OnPreferenceClickListener listener = new DataActionListener();
        exportPref.setOnPreferenceClickListener(listener);
        importPref.setOnPreferenceClickListener(listener);
        deletePref.setOnPreferenceClickListener(listener);
        changePref.setOnPreferenceClickListener(listener);
        sendPref.setOnPreferenceClickListener(listener);

        updateVisibility();
    }

    @Override
    public void onStatusChanged(DataStatus now) {
        updateVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        mModel.addListener(this);
    }

    @Override
    public void onPause() {
        super.onResume();
        mModel.removeListener(this);
    }

    private void askImportData() {
        File file = new File(EXTERNAL_DIR, FILENAME);
        if (!file.exists()) {
            showMsg("File to import not exists: " + file.getPath());
            return;
        }

        BasicInputDialog dialog = new BasicInputDialog(this);
        dialog.setTitle("Import data?");
        dialog.setMessage("Enter password to read : " + file.getPath());
        dialog.setListener(0, new BasicInputDialog.InputListener() {
            public void onInput(int id, CharSequence input, Object extra) {
                onImportData(input);
            }

            public void onCancelInput(int id, Object extra) {
            }
        });

        dialog.show();
    }

    private void onImportData(CharSequence password) {
        File input = new File(EXTERNAL_DIR, FILENAME);
        if (!input.exists()) {
            showMsg("File to import not exists:" + input.getPath());
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(input);
            List<Item> list = mModel.loadHelper(fis, password);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    mModel.addItem(list.get(i));
                }
                mModel.save();
                showMsg("Done!");
            } else {
                showMsg("Failed on loading");
            }
        } catch (Exception e) {
            showMsg("Failed");
            e.printStackTrace();
        }
    }

    private void askExportData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        File file = new File(EXTERNAL_DIR, FILENAME);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("Export data?");
        builder.setMessage("write to: " + file.getPath());
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            onExportData();
                        }
                    }
                });
        builder.show();
    }

    private void onExportData() {
        List<Item> items = mModel.getItems();
        File output = new File(EXTERNAL_DIR, FILENAME);
        if (!output.getParentFile().canWrite()) {
            showMsg("Cannot write to " + output.getPath());
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(output);
            boolean success = mModel.saveHelper(fos, items);
            if (success) {
                showMsg("Done!");
            } else {
                showMsg("Failed on writing");
            }
        } catch (Exception e) {
            showMsg("Failed");
            e.printStackTrace();
        }
    }

    private void onSendData() {
        File attachment = new File(EXTERNAL_DIR, FILENAME);

        if (!attachment.exists()) {
            showMsg("No file to send, please export data first");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        String date = sdf.format(new Date(System.currentTimeMillis()));

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "MomoMe backup encrypted data");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, date);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
        intent.setType("plain/text");
        startActivity(Intent.createChooser(intent, "Mail Chooser"));
    }

    private void askPassword() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_setpassword, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change password");
        builder.setView(view);
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            TextView t1 = (TextView) view.findViewById(R.id.pwd_oldpwd);
                            TextView t2 = (TextView) view.findViewById(R.id.pwd_newpwd1);
                            TextView t3 = (TextView) view.findViewById(R.id.pwd_newpwd2);
                            onChangePassword(t1.getText(), t2.getText(), t3.getText());
                        }
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void onChangePassword(CharSequence old, CharSequence new1, CharSequence new2) {
        if (!new1.toString().equals(new2.toString())) {
            showMsg("New passwords are not match");
            return;
        }

        if (mModel.changePassword(old, new1)) {
            showMsg("Change password successfully");
            mModel.save();
        } else {
            showMsg("The old password is not correct");
        }
    }

    private void askDeleteData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete data");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage("Are you sure to delete data of list? It CANNOT be undo");
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            checkDeleteData();
                        }
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void checkDeleteData() {
        final String answer = "" + ((int)(Math.random() * 900) + 100); // 100 ~ 999
        BasicInputDialog dialog = new BasicInputDialog(this);
        dialog.setTitle("Check again");
        dialog.setMessage("To delete data, please enter this number: " + answer);
        dialog.setListener(0, new BasicInputDialog.InputListener() {
            public void onInput(int id, CharSequence input, Object extra) {
                if (input.toString().equals(answer)) {
                    doDeleteData();
                } else {
                    showMsg("Incorrect, abort deletion");
                }
            }

            public void onCancelInput(int id, Object extra) {
            }
        });

        dialog.show();
    }

    private void doDeleteData() {
        if (mModel.delete()) {
            showMsg("Successfully delete data");
        } else {
            showMsg("Delete data failed");
        }
    }

    private void updateVisibility() {
        Preference exportPref = findPreference(KEY_EXPORT_DATA);
        Preference importPref = findPreference(KEY_IMPORT_DATA);
        Preference changePref = findPreference(KEY_CHANGE_PASSWORD);
        Preference sendPref   = findPreference(KEY_SEND_DATA);

        if (mModel.status() == DataStatus.OK) {
            exportPref.setEnabled(true);
            importPref.setEnabled(true);
            changePref.setEnabled(true);
            sendPref.setEnabled(true);
        } else {
            exportPref.setEnabled(false);
            importPref.setEnabled(false);
            changePref.setEnabled(false);
            sendPref.setEnabled(false);
        }

        if (mModel.status() == DataStatus.FILE_IS_EMPTY) {
            importPref.setEnabled(true);
        }
    }

    private void showMsg(CharSequence msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setNeutralButton(android.R.string.ok, null);
        builder.show();
    }

    class DataActionListener implements OnPreferenceClickListener {
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(KEY_EXPORT_DATA)) {
                askExportData();
            } else if (preference.getKey().equals(KEY_IMPORT_DATA)) {
                askImportData();
            } else if (preference.getKey().equals(KEY_DELETE_DATA)) {
                askDeleteData();
            } else if (preference.getKey().equals(KEY_CHANGE_PASSWORD)) {
                askPassword();
            } else if (preference.getKey().equals(KEY_SEND_DATA)) {
                onSendData();
            } else {
                Toast.makeText(PrefMain.this, "oooops",Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        }
    }
}
