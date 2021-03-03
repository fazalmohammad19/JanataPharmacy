package com.drugporter.janatapharmacy.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.locationpick.LocationGetActivity;
import com.drugporter.janatapharmacy.locationpick.MapUtility;
import com.drugporter.janatapharmacy.model.Address;
import com.drugporter.janatapharmacy.model.AddressList;
import com.drugporter.janatapharmacy.model.Pincode;
import com.drugporter.janatapharmacy.model.PincodeDatum;
import com.drugporter.janatapharmacy.model.RestResponse;
import com.drugporter.janatapharmacy.model.User;
import com.drugporter.janatapharmacy.retrofit.APIClient;
import com.drugporter.janatapharmacy.retrofit.GetResult;
import com.drugporter.janatapharmacy.utiles.CustPrograssbar;
import com.drugporter.janatapharmacy.utiles.SessionManager;
import com.drugporter.janatapharmacy.utiles.Utility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import static com.drugporter.janatapharmacy.utiles.SessionManager.pincode;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincoded;

public class AddressActivity extends RootActivity implements GetResult.MyListener {


   /* @BindView(R.id.lvl_clocation)
    LinearLayout lilCollation;*/


    @BindView(R.id.lvl_myaddress)
    LinearLayout lvlMyAddress;

    SessionManager sessionManager;
    User user;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_actiontitle)
    TextView txtActionTitle;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.my_recycler_view)
    RecyclerView myRecyclerView;

    @BindView(R.id.fbAddAddress_AddressActivity)
    FloatingActionButton fbAddAddress;

    LinearLayoutManager layoutManager;
    public static boolean changeAddress = false;
   /* @BindView(R.id.ed_pincode)
    EditText edPinCode;
    @BindView(R.id.txt_check)
    TextView txtCheck;
    @BindView(R.id.lvl_search)
    LinearLayout lvlSearch;*/
    CustPrograssbar custPrograssbar;
    private int deletedPosition = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(AddressActivity.this);
        user = sessionManager.getUserDetails("");
        layoutManager = new LinearLayoutManager(AddressActivity.this, LinearLayoutManager.VERTICAL, false);
        myRecyclerView.setLayoutManager(layoutManager);
        /*requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && Utility.hasGPSDevice(AddressActivity.this)) {
            Toast.makeText(AddressActivity.this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            Utility.enableLoc(AddressActivity.this);
        }*/
        getAddress();

        fbAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddressActivity.this, LocationGetActivity.class)
                        .putExtra(MapUtility.latitude, 0.0)
                        .putExtra(MapUtility.longitude, 0.0)
//                        .putExtra("landmark","")
//                        .putExtra("hno", "")
                        .putExtra("atype", "Home")
                        .putExtra("newuser", "curruntlat")
                        .putExtra("userid", user.getId())
                        .putExtra("aid", "0"));
            }
        });
    }

    private void getAddress() {
        custPrograssbar.prograssCreate(AddressActivity.this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getAddress(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {

            if (callNo.equalsIgnoreCase("1")) {
                custPrograssbar.closePrograssBar();
                Gson gson = new Gson();
                Address address = gson.fromJson(result.toString(), Address.class);
                if (address.getResult().equalsIgnoreCase("true")) {
                    lvlMyAddress.setVisibility(View.VISIBLE);
                    AdepterAddress adepterAddress = new AdepterAddress(AddressActivity.this, address.getAddressList());
                    myRecyclerView.setAdapter(adepterAddress);
                } else {
                    lvlMyAddress.setVisibility(View.GONE);
                }
            }else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                Toast.makeText(AddressActivity.this, response.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (response.getResult().equalsIgnoreCase("true")) {
                    if(deletedPosition < sessionManager.getIntData("position")){
                        sessionManager.setIntData("position", sessionManager.getIntData("position")-1);
                    }
                   getAddress();

                }else{
                    custPrograssbar.closePrograssBar();
                }
            }


                /*else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                Pincode pincode = gson.fromJson(result.toString(), Pincode.class);
                if (pincode.getResult().equalsIgnoreCase("true")) {
                    boolean iscode = false;
                    for (int i = 0; i < pincode.getPincodeData().size(); i++) {
                        PincodeDatum pincodeDatum = pincode.getPincodeData().get(i);
                        if (edPinCode.getText().toString().equalsIgnoreCase(pincodeDatum.getPincode())) {
                            iscode = true;
                            changeAddress = true;
                            sessionManager.setStringData(SessionManager.pincode, pincodeDatum.getId());
                            sessionManager.setStringData(pincoded, pincodeDatum.getPincode());
                            HomeActivity.getInstance().setLocation(pincodeDatum.getPincode());
                            finish();
                            break;
                        }
                    }
                    if (!iscode) {
                        Toast.makeText(AddressActivity.this, "We are currently not delivering at this location.", Toast.LENGTH_LONG).show();

                    }
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({/*R.id.lvl_clocation,*/ R.id.lvl_myaddress, R.id.img_back, /*R.id.txt_check*/})
    public void onClick(View view) {
        switch (view.getId()) {
            /*case R.id.lvl_clocation:

                startActivity(new Intent(AddressActivity.this, LocationGetActivity.class)
                        .putExtra(MapUtility.latitude, 0.0)
                        .putExtra(MapUtility.longitude, 0.0)
//                        .putExtra("landmark","")
//                        .putExtra("hno", "")
                        .putExtra("atype", "Home")
                        .putExtra("newuser", "curruntlat")
                        .putExtra("userid", user.getId())
                        .putExtra("aid", "0"));
                break;*/
            case R.id.lvl_myaddress:
                break;
            /*case R.id.txt_check:
                if (!edPinCode.getText().toString().isEmpty()) {
                    getPincode();
                } else {
                    edPinCode.setError("Enter Pincode");
                }
                break;*/
            case R.id.img_back:
                if (!sessionManager.getStringData(pincode).equalsIgnoreCase("")) {
                    finish();

                }
                break;
            default:
                break;
        }
    }


    public class AdepterAddress extends RecyclerView.Adapter<AdepterAddress.BannerHolder> {
        private Context context;
        private List<AddressList> mBanner;

        public AdepterAddress(Context context, List<AddressList> mBanner) {
            this.context = context;
            this.mBanner = mBanner;
        }

        @NonNull
        @Override
        public BannerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.adapter_addresss_item, parent, false);
            return new BannerHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerHolder holder, int position) {

            holder.txtType.setText("" + mBanner.get(position).getType());
            holder.txtHomeaddress.setText(mBanner.get(position).getHno());
            holder.tvAddress.setText(mBanner.get(position).getAddress());
            //Glide.with(context).load(APIClient.baseUrl + "/" + mBanner.get(position).getAddressImage()).thumbnail(Glide.with(context).load(R.drawable.ezgifresize)).centerCrop().into(holder.imgBanner);
            /*holder.lvlHome.setOnClickListener(v -> {

            });*/

            if(position == sessionManager.getIntData("position")){
                holder.btnDefault.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_default_filled, 0, 0, 0);
                holder.btnDefault.setText("Default");
            }else{
                holder.btnDefault.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_default_unfilled, 0, 0, 0);
                holder.btnDefault.setText("Set as default");
            }

            holder.btnDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeAddress = true;
                    sessionManager.setIntData("position", position);
                    sessionManager.setStringData(pincode, mBanner.get(position).getPincodeId());
                    sessionManager.setStringData(pincoded, mBanner.get(position).getAddress());
                    notifyDataSetChanged();
                }
            });

            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(AddressActivity.this, LocationGetActivity.class)
                            .putExtra(MapUtility.latitude, mBanner.get(position).getLatMap())
                            .putExtra(MapUtility.longitude, mBanner.get(position).getLongMap())
                            .putExtra("atype", mBanner.get(position).getType())
                            .putExtra("landmark", mBanner.get(position).getLandmark())
                            .putExtra("hno", mBanner.get(position).getHno())
                            .putExtra("newuser", "update")
                            .putExtra("userid", user.getId())
                            .putExtra("aid", mBanner.get(position).getId()));
                }
            });

            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(position == sessionManager.getIntData("position")){
                        Toast.makeText(context, "Can not delete Default address!", Toast.LENGTH_SHORT).show();
                    }else{
                        deletedPosition = position;
                        custPrograssbar.prograssCreate(AddressActivity.this);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("aid", mBanner.get(position).getId());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                        Call<JsonObject> call = APIClient.getInterface().deleteAddress(bodyRequest);
                        GetResult getResult = new GetResult();
                        getResult.setMyListener(AddressActivity.this);
                        getResult.callForLogin(call, "2");
                    }


                }
                });


           /*
            holder.imgMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.imgMenu);
                popup.inflate(R.menu.address_menu);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_select:
                            changeAddress = true;
                            sessionManager.setIntData("position", position);
                            sessionManager.setStringData(pincode, mBanner.get(position).getPincodeId());
                            sessionManager.setStringData(pincoded, mBanner.get(position).getAddress());
                            finish();
                            break;
                        case R.id.menu_edit:
                            startActivity(new Intent(AddressActivity.this, LocationGetActivity.class)
                                    .putExtra(MapUtility.latitude, mBanner.get(position).getLatMap())
                                    .putExtra(MapUtility.longitude, mBanner.get(position).getLongMap())
                                    .putExtra("atype", mBanner.get(position).getType())
                                    .putExtra("landmark", mBanner.get(position).getLandmark())
                                    .putExtra("hno", mBanner.get(position).getHno())
                                    .putExtra("newuser", "update")
                                    .putExtra("userid", user.getId())
                                    .putExtra("aid", "0"));

                            break;
                        default:
                            break;
                    }
                    return false;
                });
                popup.show();
            });*/

        }

        @Override
        public int getItemCount() {
            return mBanner.size();
        }

        public class BannerHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.img_banner)
            ImageView imgBanner;
            @BindView(R.id.img_menu)
            ImageView imgMenu;
            @BindView(R.id.txt_homeaddress)
            TextView txtHomeaddress;
            @BindView(R.id.txt_tital)
            TextView txtType;

            @BindView(R.id.btnEdit_AddressActivtiy)
            TextView btnEdit;

            @BindView(R.id.btnDefault_AddressActivtiy)
            TextView btnDefault;

            @BindView(R.id.tvAddress_AddressActivtiy)
            TextView tvAddress;

            @BindView(R.id.ivDelete_AddressActivtiy)
            ImageView ivDelete;
            /*@BindView(R.id.lvl_home)
            LinearLayout lvlHome;*/

            public BannerHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    private void getPincode() {
        custPrograssbar.prograssCreate(AddressActivity.this);

        JSONObject jsonObject = new JSONObject();
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getPinCode(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null) {
            getAddress();
        }
    }

    @Override
    public void onBackPressed() {
        if (!sessionManager.getStringData(pincode).equalsIgnoreCase("")) {
            super.onBackPressed();
        }
    }
}