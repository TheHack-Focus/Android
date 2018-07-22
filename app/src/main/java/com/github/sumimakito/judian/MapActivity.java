package com.github.sumimakito.judian;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MapActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener {
    private static final String TAG = "MapActivity";
    private static final int PERMISSION_REQUEST_CODE = 0xCee;
    private MapView mapView;
    private boolean viewInitialized = false;
    private AMap aMap;
    private FloatingActionButton centerLocationFab;
    private FloatingActionButton newPostFab;
    private Location lastLocation = null;
    private DrawerLayout navDrawerLayout;
    private NavigationView navDrawerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageButton menuButton;
    private boolean liveInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.map_map_view);
        centerLocationFab = findViewById(R.id.map_center_location_fab);
        newPostFab = findViewById(R.id.map_new_post_fab);
        navDrawerLayout = findViewById(R.id.map_drawer_layout);
        navDrawerView = findViewById(R.id.map_nav_view);
        View header = navDrawerView.inflateHeaderView(R.layout.include_nav_drawer);
        SimpleDraweeView profileImage = header.findViewById(R.id.profile_image);
        TextView usernameText = header.findViewById(R.id.username);
        usernameText.setText(App.username);
        Uri uri = Uri.parse("https://hackshdemowk1.eastasia.cloudapp.azure.com/account/avatar/" + App.username);
        profileImage.setImageURI(uri);
        navDrawerView.inflateMenu(R.menu.menu_drawer);
        menuButton = findViewById(R.id.map_menu_button);
        menuButton.setOnClickListener(v -> {
            if (navDrawerLayout.isDrawerOpen(Gravity.START)) {
                navDrawerLayout.closeDrawer(Gravity.START);
            } else {
                navDrawerLayout.openDrawer(Gravity.START);
            }
        });
        newPostFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewPostActivity.class);
            if (lastLocation != null) {
                intent.putExtra("latlon", new double[]{
                        lastLocation.getLatitude(), lastLocation.getLongitude()
                });
            }
            startActivity(intent);
        });
        if (checkPermission()) {
            initViews();
        }
    }

    private void initViews() {
        if (viewInitialized) return;
        viewInitialized = true;
        mapView.onCreate(new Bundle());
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.setOnMyLocationChangeListener(this);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMyLocationEnabled(true);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(500);
        myLocationStyle.strokeColor(Color.parseColor("#cca4deff"));
        myLocationStyle.radiusFillColor(Color.parseColor("#66a4deff"));
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
        aMap.setMyLocationStyle(myLocationStyle);
        centerLocationFab.setOnClickListener(v -> {
            try {
                if (lastLocation != null) {
                    animateTo(lastLocation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String uuid = marker.getTitle();
                for (Card c : App.cards) {
                    if (c.uuid.equals(uuid)) {
                        new CardDialogFragment(c).show(getSupportFragmentManager(), "CardDialogFragment");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void addMarker(Card card) {
        Log.w(TAG, "addMarker");
        if (card.added) return;
        Log.w(TAG, "addMarker: Cont");
        new Thread(() -> {
            try {
                String urlPath = "https://hackshdemowk1.eastasia.cloudapp.azure.com/account/avatar/" + card.username;
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(urlPath).getContent());
                Bitmap processed = PinProcessor.process(this, bitmap);

                MarkerOptions markerOption = new MarkerOptions();
                markerOption.title(card.uuid);
                markerOption.position(new LatLng(card.latlon[0], card.latlon[1]));
                markerOption.icon(BitmapDescriptorFactory.fromBitmap(processed));
                aMap.addMarker(markerOption);
                card.added = true;
                Log.w(TAG, "addMarker: Added");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
        if (aMap != null) {
            for (Card c : App.cards) {
                addMarker(c);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(MapActivity.this)
                            .setMessage("为了使程序正常运行，请授予定位权限。")
                            .setPositiveButton("继续", (d, v) -> {
                                d.dismiss();
                                if (checkPermission()) {
                                    initViews();
                                }
                            })
                            .setNegativeButton("退出", (d, v) -> {
                                d.dismiss();
                                this.finish();
                            })
                            .setCancelable(false)
                            .create().show();
                    return;
                }
            }
            initViews();
        }
    }

    private boolean checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    private void animateTo(Location location) {
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                new CameraPosition(
                        new LatLng(location.getLatitude(), location.getLongitude()),
                        aMap.getCameraPosition().zoom, aMap.getCameraPosition().tilt, aMap.getCameraPosition().bearing
                )
        );
        aMap.animateCamera(mCameraUpdate);
    }

    @Override
    public void onMyLocationChange(Location location) {
        boolean autoRelocation = lastLocation == null;
        // lastLocation = new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude());
        lastLocation = location;
        if (autoRelocation) {
            animateTo(location);
        }
        if (!liveInitialized) {
            liveInitialized = true;
            JudianClient.live(new double[]{lastLocation.getLatitude(), lastLocation.getLongitude()}, 10000, new JudianClient.ResultCallback() {
                @Override
                public void onResult(String result) {
                    Log.w(TAG, "onResult: " + result);
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Card card = new Card();
                            card.uuid = jsonObject.getString("id");
                            card.ts = jsonObject.getString("timeStamp").split("\\.")[0].replace("T", " ");
                            card.type = Card.Type.Incoming;
                            card.latlon = new double[]{jsonObject.getJSONObject("location").getDouble("lat"), jsonObject.getJSONObject("location").getDouble("lon")};
                            if (jsonObject.has("text")) {
                                card.content = jsonObject.getString("text");
                            }
                            if (jsonObject.has("title")) {
                                card.title = jsonObject.getString("title");
                            }

                            card.username = jsonObject.getJSONObject("publisher").getString("userName");
                            try {
                                if (jsonObject.has("media") && jsonObject.get("media") != null) {
                                    JSONArray images = jsonObject.getJSONObject("media").getJSONArray("images");
                                    for (int j = 0; j < images.length(); j++) {
                                        Attachment attachment = new Attachment();
                                        attachment.url = images.getString(j);
                                        attachment.mimeType = "image/*";
                                        card.attachments.add(attachment);
                                    }
                                }
                            } catch (Exception e) {
                            }
                            App.cards.add(card);
                            addMarker(card);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailed() {
                    Log.e(TAG, "Failed to retrieve initial data.");
                }
            });
        }
    }

    @SuppressLint("ValidFragment")
    static class CardDialogFragment extends DialogFragment {
        private final Card dialogCard;
        private CardAttachmentsAdapter adapter;
        private RecyclerView recyclerView;
        private Button addButton;
        private ImageButton likeButton;
        private TextView metadataText, titleText, contentText;

        CardDialogFragment(Card card) {
            this.dialogCard = card;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NO_TITLE,
                    android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View contentView = View.inflate(getContext(), R.layout.include_card, null);
            likeButton = contentView.findViewById(R.id.card_like);
            metadataText = contentView.findViewById(R.id.card_metadata);
            titleText = contentView.findViewById(R.id.card_title);
            contentText = contentView.findViewById(R.id.card_content);
            recyclerView = contentView.findViewById(R.id.card_recycler);
            titleText.setText(dialogCard.title);
            metadataText.setText(dialogCard.username + " " + dialogCard.ts);

            if (dialogCard.content == null || dialogCard.content.isEmpty() || dialogCard.content == "null") {
                contentText.setVisibility(View.GONE);
            } else {
                contentText.setVisibility(View.VISIBLE);
                contentText.setText(dialogCard.content);
            }
            recyclerView.setVisibility(dialogCard.attachments.size() == 0 ? View.GONE : View.VISIBLE);
            adapter = new CardAttachmentsAdapter(getContext(), dialogCard);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);
            likeButton.setImageDrawable(dialogCard.liked ? getResources().getDrawable(R.drawable.ic_favorite_mikan_24dp) : getResources().getDrawable(R.drawable.ic_favorite_grey_24dp));
            likeButton.setOnClickListener(v -> {
                dialogCard.likeCount += dialogCard.liked ? -1 : 1;
                dialogCard.liked = !dialogCard.liked;
                likeButton.setImageDrawable(dialogCard.liked ? getResources().getDrawable(R.drawable.ic_favorite_mikan_24dp) : getResources().getDrawable(R.drawable.ic_favorite_grey_24dp));
            });
            Dialog dialog = new AlertDialog.Builder(getContext()).setView(contentView).setNegativeButton("关闭", (d, v) -> d.dismiss()).create();
            return dialog;
        }
    }

    public static class CardAttachmentsAdapter extends RecyclerView.Adapter<CardAttachmentsAdapter.AttachmentItemHolder> {

        private final LayoutInflater mLayoutInflater;

        private static CardAttachmentsAdapter.OnItemEventListener onItemEventListener;
        private final Card adapterCard;
        private final Context mContext;

        CardAttachmentsAdapter(Context context, Card card) {
            this.mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            this.adapterCard = card;
        }

        @Override
        public CardAttachmentsAdapter.AttachmentItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CardAttachmentsAdapter.AttachmentItemHolder(mLayoutInflater.inflate(R.layout.include_attachment_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final CardAttachmentsAdapter.AttachmentItemHolder holder, final int position) {
            Attachment attachment = adapterCard.attachments.get(position);
            try {
                String[] parts = attachment.url.split("/");
                holder.textFilename.setText(parts[parts.length - 1]);
                holder.textMime.setText(attachment.mimeType);
                if (adapterCard.type == Card.Type.Incoming) {
                    if (attachment.mimeType.startsWith("image/")) {
                        holder.image.setImageURI(Uri.parse(attachment.url));
                    } else {
                        holder.image.setImageResource(R.drawable.placeholder);
                    }
                } else {
                    if (attachment.mimeType.startsWith("image/")) {
                        holder.image.setImageURI(Uri.fromFile(attachment.file));
                    } else {
                        holder.image.setImageResource(R.drawable.placeholder);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setOnItemClickListener(CardAttachmentsAdapter.OnItemEventListener listener) {
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
