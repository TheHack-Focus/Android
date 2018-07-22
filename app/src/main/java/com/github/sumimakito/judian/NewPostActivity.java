package com.github.sumimakito.judian;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.sumimakito.cappuccino.util.ContentHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NewPostActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 0xCee;
    private static final int OPEN_FILE_REQUEST_CODE = 0xeeC;
    private TextInputEditText usernameEdit, passwordEdit;
    private MaterialButton loginButton;
    private FloatingActionButton attachmentFab, sendFab;
    private TextView badgeText;
    private EditText titleText, contentText;
    private ImageButton backButton;
    private Card card;
    private View secondaryRoot;
    private double[] latlon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        if (getIntent() != null && getIntent().getDoubleArrayExtra("latlon") != null) {
            latlon = getIntent().getDoubleArrayExtra("latlon");
        } else {
            Toast.makeText(this, "未取得坐标数据", Toast.LENGTH_SHORT).show();
            this.finish();
        }

        secondaryRoot = findViewById(R.id.new_post_secondary_root);

        backButton = findViewById(R.id.new_post_back);

        attachmentFab = findViewById(R.id.new_post_attachment);
        sendFab = findViewById(R.id.new_post_send);
        badgeText = findViewById(R.id.new_post_badge);

        titleText = findViewById(R.id.new_post_title);
        contentText = findViewById(R.id.new_post_content);

        card = new Card();
        card.type = Card.Type.Outgoing;
        card.username = App.username;
        card.latlon = latlon;
        card.uuid = UUID.randomUUID().toString();

        backButton.setOnClickListener(v -> finish());
        attachmentFab.setOnClickListener(v -> {
            if (card.attachments.size() == 0)
                selectFile();
            else {
                new AttachmentListBSDialogFragment(() -> {
                    badgeText.setText(String.valueOf(card.attachments.size()));
                    badgeText.setVisibility(card.attachments.size() > 0 ? View.VISIBLE : View.GONE);
                }, card).show(getSupportFragmentManager(), "AttachmentListBSDialogFragment");
            }
        });

        sendFab.setOnClickListener(v -> {
            card.title = titleText.getText().toString();
            card.content = contentText.getText().toString();
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            card.ts = outFormat.format(new Date());
            final android.app.AlertDialog dialog = new ProgressDialog.Builder(this)
                    .setMessage("发送中…")
                    .setCancelable(false)
                    .create();
            dialog.show();
            JudianClient.post(latlon, card.title.isEmpty() ? null : card.title, card.content.isEmpty() ? null : card.content, "", new JudianClient.ResultCallback() {
                @Override
                public void onResult(String result) {
                    dialog.dismiss();
                    App.cards.add(card);
                    NewPostActivity.this.finish();
                    runOnUiThread(() -> Toast.makeText(NewPostActivity.this, "发送成功", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onFailed() {
                    dialog.dismiss();
                    runOnUiThread(() -> Toast.makeText(NewPostActivity.this, "发送失败, 请重新尝试", Toast.LENGTH_SHORT).show());
                }
            });
        });

        checkPermission();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
    }

    private void update() {
        badgeText.setText(card.attachments.size() + "");
        badgeText.setVisibility(card.attachments.size() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(NewPostActivity.this)
                            .setMessage("为了使程序正常运行，请授予存储权限。")
                            .setPositiveButton("继续", (d, v) -> {
                                d.dismiss();
                                checkPermission();
                            })
                            .setNegativeButton("取消", (d, v) -> {
                                d.dismiss();
                                this.finish();
                            })
                            .setCancelable(false)
                            .create().show();
                    return;
                }
            }
        }
    }

    public static String getMimeType(String fileUrl) {
        try {
            URL u = new URL(fileUrl);
            URLConnection uc = u.openConnection();
            return uc.getContentType();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data.getData() != null) {
                try {
                    Uri uri = data.getData();
                    if (uri != null) {
                        Attachment attachment = new Attachment();
                        attachment.file = new File(ContentHelper.absolutePathFromUri(this, uri));
                        attachment.mimeType = getMimeType("file://" + attachment.file.getAbsolutePath());
                        card.attachments.add(attachment);
                        Toast.makeText(NewPostActivity.this, "文件已添加", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(NewPostActivity.this, "文件添加失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(NewPostActivity.this, "文件添加失败", Toast.LENGTH_SHORT).show();
            }
        }

        badgeText.setText(card.attachments.size() + "");
        badgeText.setVisibility(card.attachments.size() > 0 ? View.VISIBLE : View.GONE);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("ValidFragment")
    static class AttachmentListBSDialogFragment extends BottomSheetDialogFragment {
        private static final String TAG = "ScanResultBottomSheetDi";

        private final DismissCallback callback;
        private final Card dialogCard;
        private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    Log.w(TAG, "onStateChanged: dismiss");
                    dismiss();
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        };
        private AttachmentListBSRecyclerAdapter adapter;
        private RecyclerView recyclerView;
        private Button addButton;

        AttachmentListBSDialogFragment(DismissCallback callback, Card card) {
            this.callback = callback;
            this.dialogCard = card;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            Log.w(TAG, "onDismiss");
            callback.onDismiss();
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void setupDialog(Dialog dialog, int style) {
            super.setupDialog(dialog, style);
            View contentView = View.inflate(getContext(), R.layout.include_attachment_list, null);
            addButton = contentView.findViewById(R.id.attachment_list_add);
            recyclerView = contentView.findViewById(R.id.attachment_list_recycler);
            adapter = new AttachmentListBSRecyclerAdapter(getContext(), dialogCard);
            adapter.setOnItemClickListener((v, p) -> {
                dialogCard.attachments.remove(p);
                adapter.notifyDataSetChanged();
                ((NewPostActivity) getActivity()).update();
                if (dialogCard.attachments.size() == 0) dismiss();
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);
            addButton.setOnClickListener(v -> {
                ((NewPostActivity) getActivity()).selectFile();
                dismiss();
            });
            dialog.setContentView(contentView);

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();

            if (behavior instanceof BottomSheetBehavior) {
                ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            }
        }

        public interface DismissCallback {
            void onDismiss();
        }
    }

    public static class AttachmentListBSRecyclerAdapter extends RecyclerView.Adapter<AttachmentListBSRecyclerAdapter.AttachmentItemHolder> {

        private final LayoutInflater mLayoutInflater;

        private static OnItemEventListener onItemEventListener;
        private final Card adapterCard;

        AttachmentListBSRecyclerAdapter(Context context, Card card) {
            mLayoutInflater = LayoutInflater.from(context);
            this.adapterCard = card;
        }

        @Override
        public AttachmentItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AttachmentItemHolder(mLayoutInflater.inflate(R.layout.include_attachment_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final AttachmentItemHolder holder, final int position) {
            Attachment attachment = adapterCard.attachments.get(position);
            try {
                holder.textFilename.setText(attachment.file.getName());
                holder.textMime.setText(attachment.mimeType);
                if (attachment.mimeType.startsWith("image/")) {
                    holder.image.setImageURI(Uri.fromFile(attachment.file));
                } else {
                    holder.image.setImageResource(R.drawable.placeholder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setOnItemClickListener(OnItemEventListener listener) {
            onItemEventListener = listener;
        }

        @Override
        public int getItemCount() {
            return adapterCard.attachments.size();
        }

        class AttachmentItemHolder extends RecyclerView.ViewHolder {

            TextView textFilename;
            TextView textMime;
            SimpleDraweeView image;

            AttachmentItemHolder(View view) {
                super(view);
                textFilename = view.findViewById(R.id.attachment_item_name);
                textMime = view.findViewById(R.id.attachment_item_mime);
                image = view.findViewById(R.id.attachment_item_image);
                view.setOnClickListener(v -> {
                    if (onItemEventListener != null)
                        onItemEventListener.onClick(v, getLayoutPosition());
                });
            }
        }

        interface OnItemEventListener {
            void onClick(View v, int p);
        }
    }
}
