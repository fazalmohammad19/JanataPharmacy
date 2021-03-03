package com.drugporter.janatapharmacy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.imagepicker.ImagePicker;
import com.drugporter.janatapharmacy.locationpick.LocationGetActivity;
import com.drugporter.janatapharmacy.locationpick.MapUtility;
import com.drugporter.janatapharmacy.model.Address;
import com.drugporter.janatapharmacy.model.AddressList;
import com.drugporter.janatapharmacy.model.RestResponse;
import com.drugporter.janatapharmacy.model.User;
import com.drugporter.janatapharmacy.retrofit.APIClient;
import com.drugporter.janatapharmacy.retrofit.GetResult;
import com.drugporter.janatapharmacy.utiles.CustPrograssbar;
import com.drugporter.janatapharmacy.utiles.FileUtils;
import com.drugporter.janatapharmacy.utiles.SessionManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.drugporter.janatapharmacy.ui.AddressActivity.changeAddress;
import static com.drugporter.janatapharmacy.utiles.SessionManager.address;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincode;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincoded;

public class OrderByTyping extends AppCompatActivity implements GetResult.MyListener {
    @BindView(R.id.img_back_oder_by_typing)
    ImageView imgBack;
    @BindView(R.id.txt_actiontitle_oder_by_typing)
    TextView txtActiontitle;
    @BindView(R.id.toolbar_oder_by_typing)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout_oder_by_typing)
    AppBarLayout appBarLayout;

    @BindView(R.id.lvl_pic_oder_by_typing)
    LinearLayout lvlPic;

    @BindView(R.id.txt_atype_oder_by_typing)
    TextView txtAtype;
    @BindView(R.id.txt_address_oder_by_typing)
    TextView txtAddress;
    @BindView(R.id.txt_changeadress_oder_by_typing)
    TextView txtChangeadress;

    @BindView(R.id.btn_submit_oder_by_typing)
    TextView btnSubmit;
    @BindView(R.id.etTextPrescription_oder_by_typing)
    EditText etTextPrescription;

    CustPrograssbar custPrograssbar;
    User user;
    SessionManager sessionManager;
    Address addresses;
    static AdepterAddress adepterAddress;
    static RecyclerView rvAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_by_typing);

        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        addresses = new Address();
        sessionManager = new SessionManager(OrderByTyping.this);
        user = sessionManager.getUserDetails("");


        getAddress();

    }

    @OnClick({R.id.img_back_oder_by_typing, R.id.btn_submit_oder_by_typing, R.id.changeAddressLayout_oder_by_typing})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back_oder_by_typing:
                finish();
                break;
            case R.id.btn_submit_oder_by_typing:
                SubmitPrescriptionOrder();
                break;
            case R.id.changeAddressLayout_oder_by_typing:
                /*startActivity(new Intent(UploadPrescriptionActivity.this, AddressActivity.class));*/
                bottomSheetForAddress();
                break;

            default:
                break;
        }
    }

    private void SubmitPrescriptionOrder() {
        if(TextUtils.isEmpty(etTextPrescription.getText().toString()) || etTextPrescription.getText().length()<2){
            Toast.makeText(this, "Enter Valid Prescription!", Toast.LENGTH_SHORT).show();
            return;
        }
        custPrograssbar.prograssCreate(OrderByTyping.this);
        String userid =user.getId();
        String add = sessionManager.getStringData(address)+"";

       /* RequestBody uid = createPartFromString(user.getId());
        RequestBody address_id = createPartFromString(sessionManager.getStringData(address));
        RequestBody note = createPartFromString(etTextPrescription.getText().toString());
        RequestBody size = createPartFromString("size");
        RequestBody part = createPartFromString("part");*/

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("Full_Address", sessionManager.getStringData(address));
            jsonObject.put("size", "size");
            jsonObject.put("part", "part");
            jsonObject.put("note", etTextPrescription.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();

        }
        /*RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getAddress(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");*/

        // finally, execute the request
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().submitTextPrescription(bodyRequest);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                custPrograssbar.closePrograssBar();
                Gson gson = new Gson();
                RestResponse restResponse = gson.fromJson(response.body(), RestResponse.class);
                Toast.makeText(OrderByTyping.this, restResponse.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (restResponse.getResult().equalsIgnoreCase("true")) {
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(OrderByTyping.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                custPrograssbar.closePrograssBar();

            }
        });
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_TEXT), descriptionString);
    }

    public void bottonVelidation() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.custome_vallid_layout, null);
        mBottomSheetDialog.setContentView(sheetView);


        mBottomSheetDialog.show();
    }

    public void bottomSheetForAddress() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.address_selection_bottomsheet_layout, null);
        mBottomSheetDialog.setContentView(sheetView);

        TextView tvAddAddress = sheetView.findViewById(R.id.tvAddAddress_AddressSelectionBottomSheet);
        TextView tvClose = sheetView.findViewById(R.id.tvClose_AddressSelectionBottomSheet);
        rvAddresses = sheetView.findViewById(R.id.rvAddresses_AddressSelectionBottomSheet);
        LinearLayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(OrderByTyping.this, LinearLayoutManager.VERTICAL, false);
        rvAddresses.setLayoutManager(layoutManager);
        adepterAddress = new AdepterAddress(OrderByTyping.this, addresses.getAddressList());
        rvAddresses.setAdapter(adepterAddress);

        mBottomSheetDialog.show();

       /* tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.cancel();
            }
        });*/

        tvAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAddress = true;
                startActivity(new Intent(OrderByTyping.this, LocationGetActivity.class)
                        .putExtra(MapUtility.latitude, 0.0)
                        .putExtra(MapUtility.longitude, 0.0)
                        .putExtra("atype", "Home")
                        .putExtra("newuser", "curruntlat")
                        .putExtra("userid", user.getId())
                        .putExtra("aid", "0"));

                mBottomSheetDialog.cancel();
            }
        });


    }

    private void getAddress() {
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
        Gson gson = new Gson();
        if(callNo.equalsIgnoreCase("1")){
            addresses = gson.fromJson(result.toString(), Address.class);
            if (addresses.getResult().equalsIgnoreCase("true")) {
                if (addresses.getAddressList().size() != 0 && sessionManager.getIntData("position") >-1) {
                    String size = addresses.getAddressList().size()+"";
                    String pos = sessionManager.getIntData("position")+"";
                    if(!(addresses.getAddressList().size()-1<sessionManager.getIntData("position"))){
                        if(changeAddress){
                            sessionManager.setIntData("position", addresses.getAddressList().size()-1);
                            sessionManager.setStringData(pincode, addresses.getAddressList().get(addresses.getAddressList().size()-1).getPincodeId());
                            txtAtype.setText("" + addresses.getAddressList().get(addresses.getAddressList().size()-1).getType());
                            txtAddress.setText("" + addresses.getAddressList().get(addresses.getAddressList().size()-1).getAddress());
                            sessionManager.setStringData(SessionManager.address, addresses.getAddressList().get(addresses.getAddressList().size()-1).getAddress());
                        }else{
                            txtAtype.setText("" + addresses.getAddressList().get(sessionManager.getIntData("position")).getType());
                            txtAddress.setText("" + addresses.getAddressList().get(sessionManager.getIntData("position")).getAddress());
                            sessionManager.setStringData(SessionManager.address, addresses.getAddressList().get(sessionManager.getIntData("position")).getAddress());
                        }
                    }

                } else {
                    Toast.makeText(OrderByTyping.this, addresses.getResponseMsg(), Toast.LENGTH_SHORT).show();
                }

            } else {
                // finish();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager == null) {
            return;
        }
        if (changeAddress) {
            changeAddress = false;
            getAddress();
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
        public AdepterAddress.BannerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.address_list_item_bottomsheet_layout, parent, false);
            return new AdepterAddress.BannerHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdepterAddress.BannerHolder holder, int position) {

            holder.txtType.setText("" + mBanner.get(position).getType());
            holder.txtHomeaddress.setText(mBanner.get(position).getHno());
            holder.tvAddress.setText(mBanner.get(position).getAddress());
            //Glide.with(context).load(APIClient.baseUrl + "/" + mBanner.get(position).getAddressImage()).thumbnail(Glide.with(context).load(R.drawable.ezgifresize)).centerCrop().into(holder.imgBanner);
            /*holder.lvlHome.setOnClickListener(v -> {

            });*/


            if(position == sessionManager.getIntData("position")){
                holder.checkBox.setImageDrawable(getDrawable(R.drawable.ic_checked));
            }else{
                holder.checkBox.setImageDrawable(getDrawable(R.drawable.unchecked));
            }

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(position != sessionManager.getIntData("position")){
                        sessionManager.setIntData("position", position);
                        sessionManager.setStringData(pincode, mBanner.get(position).getPincodeId());
                        sessionManager.setStringData(pincoded, mBanner.get(position).getAddress());
                        txtAtype.setText("" + addresses.getAddressList().get(sessionManager.getIntData("position")).getType());
                        txtAddress.setText("" + addresses.getAddressList().get(sessionManager.getIntData("position")).getAddress());
                        sessionManager.setStringData(SessionManager.address, addresses.getAddressList().get(sessionManager.getIntData("position")).getAddress());
                        notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mBanner.size();
        }

        public class BannerHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.img_banner)
            ImageView imgBanner;
            @BindView(R.id.txt_homeaddress)
            TextView txtHomeaddress;
            @BindView(R.id.txt_tital)
            TextView txtType;

            @BindView(R.id.cbChooseDefault_ListItem)
            ImageView checkBox;

            @BindView(R.id.tvAddress_AddressActivtiy)
            TextView tvAddress;
            /*@BindView(R.id.lvl_home)
            LinearLayout lvlHome;*/

            public BannerHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}