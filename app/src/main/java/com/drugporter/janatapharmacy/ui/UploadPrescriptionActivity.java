package com.drugporter.janatapharmacy.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.imagepicker.ImageCompressionListener;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.drugporter.janatapharmacy.ui.AddressActivity.changeAddress;
import static com.drugporter.janatapharmacy.utiles.FileUtils.isLocal;
import static com.drugporter.janatapharmacy.utiles.SessionManager.address;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincode;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincoded;

public class UploadPrescriptionActivity extends RootActivity implements GetResult.MyListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_actiontitle)
    TextView txtActiontitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.txt_prescription_valid)
    TextView txtPrescriptionValid;
    @BindView(R.id.btn_upload)
    TextView btnUpload;
    @BindView(R.id.lvl_empty)
    LinearLayout lvlEmpty;
    @BindView(R.id.lvl_pic)
    LinearLayout lvlPic;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.txt_atype)
    TextView txtAtype;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.txt_changeadress)
    TextView txtChangeadress;
    @BindView(R.id.btn_ather)
    TextView btnAther;
    @BindView(R.id.btn_submit)
    TextView btnSubmit;
    @BindView(R.id.etTextPrescription_UploadPrescriptionActivity)
    EditText etTextPrescription;
    private ImagePicker imagePicker;
    ArrayList<String> arrayListImage = new ArrayList<>();
    CustPrograssbar custPrograssbar;
    User user;
    SessionManager sessionManager;
    Address addresses;
    static AdepterAddress adepterAddress;
    static RecyclerView rvAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_prescription);
        ButterKnife.bind(this);

        custPrograssbar = new CustPrograssbar();
        addresses = new Address();
        sessionManager = new SessionManager(UploadPrescriptionActivity.this);
        user = sessionManager.getUserDetails("");
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(this);
        mLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        imagePicker = new ImagePicker();
        if (checkPermission()) {
            //start image picker

        } else {
            //ask permission
            requestStoragePermission();
        }
        getAddress();

    }

    private void uploadMultiFile(ArrayList<String> filePaths) {
        custPrograssbar.prograssCreate(UploadPrescriptionActivity.this);
        List<MultipartBody.Part> parts = new ArrayList<>();

        if (filePaths != null) {
            // create part for file (photo, video, ...)
            for (int i = 0; i < filePaths.size(); i++) {
                parts.add(prepareFilePart("image" + i, filePaths.get(i)));
            }
        }
// create a map of data to pass along

        String userid =user.getId();
        String add = sessionManager.getStringData(address)+"";
        String s = "" + parts.size();

        RequestBody uid = createPartFromString(user.getId());
        RequestBody address_id = createPartFromString(sessionManager.getStringData(address));
        RequestBody size = createPartFromString("" + parts.size());
        RequestBody note = createPartFromString(etTextPrescription.getText().toString());

// finally, execute the request
        Call<JsonObject> call = APIClient.getInterface().uploadMultiFile(uid, address_id, size, parts, note);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                custPrograssbar.closePrograssBar();
                Gson gson = new Gson();
                RestResponse restResponse = gson.fromJson(response.body(), RestResponse.class);
                Toast.makeText(UploadPrescriptionActivity.this, restResponse.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (restResponse.getResult().equalsIgnoreCase("true")) {
                    lvlEmpty.setVisibility(VISIBLE);
                    lvlPic.setVisibility(GONE);
                    arrayListImage.clear();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                custPrograssbar.closePrograssBar();

            }
        });

    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_TEXT), descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String fileUri) {
        // use the FileUtils to get the actual file by uri
        File file = getFile(UploadPrescriptionActivity.this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public static File getFile(Context context, String path) {
        if (path == null) {
            return null;
        }

        if (isLocal(path)) {
            return new File(path);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.SELECT_IMAGE && resultCode == RESULT_OK) {

                imagePicker.addOnCompressListener(new ImageCompressionListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onCompressed(String filePath) {
                        if (filePath != null) {
                            //return filepath
                            arrayListImage.add(filePath);
                            postImage(arrayListImage);
                        }
                    }
                });
                String filePath = imagePicker.getImageFilePath(data);
                if (filePath != null) {
                    //return filepath
                    arrayListImage.add(filePath);
                    postImage(arrayListImage);
                }

        }
    }

    public void postImage(ArrayList<String> urilist) {
        if (urilist.size() != 0) {
            lvlEmpty.setVisibility(GONE);
        }
        ImageAdp imageAdp = new ImageAdp(UploadPrescriptionActivity.this, urilist);
        recyclerView.setAdapter(imageAdp);

    }

    @OnClick({R.id.img_back, R.id.txt_prescription_valid, R.id.btn_upload, R.id.btn_ather, R.id.btn_submit, R.id.changeAddressLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_ather:
                bottonChoseoption();
                break;
            case R.id.btn_submit:
                if (arrayListImage.size() != 0) {
                    uploadMultiFile(arrayListImage);
                }
                break;
            case R.id.changeAddressLayout:
                /*startActivity(new Intent(UploadPrescriptionActivity.this, AddressActivity.class));*/
                bottomSheetForAddress();

                break;
            case R.id.txt_prescription_valid:
                bottonVelidation();

                break;
            case R.id.btn_upload:
                bottonChoseoption();
                break;
            default:
                break;
        }
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        Gson gson = new Gson();
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


                /*if(recyclerView != null && adepterAddress!= null){
                    recyclerView.setAdapter(adepterAddress);
                }*/

            } else {
                Toast.makeText(UploadPrescriptionActivity.this, addresses.getResponseMsg(), Toast.LENGTH_SHORT).show();
            }

        } else {
           // finish();
        }
    }


    public class ImageAdp extends RecyclerView.Adapter<ImageAdp.MyViewHolder> {
        private ArrayList<String> arrayList;


        public class MyViewHolder extends RecyclerView.ViewHolder {

            public ImageView remove;
            public ImageView thumbnail;

            public MyViewHolder(View view) {
                super(view);

                thumbnail = view.findViewById(R.id.image_pic);
                remove = view.findViewById(R.id.image_remove);
            }
        }

        public ImageAdp(Context mContext, ArrayList<String> arrayList) {
            this.arrayList = arrayList;

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.imageview_layout, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            ImagePopup imagePopup = new ImagePopup(UploadPrescriptionActivity.this);
            imagePopup.setFullScreen(true);
            // Optional
            imagePopup.setHideCloseIcon(false);
            // Optional
            imagePopup.setImageOnClickClose(false);
            // Optional

            Glide.with(UploadPrescriptionActivity.this)
                    .load(arrayList.get(position))
                    .into(holder.thumbnail);
            holder.remove.setOnClickListener(v -> {
                arrayList.remove(position);
                if (arrayList.size() != 0) {
                    notifyDataSetChanged();
                } else {
                    lvlEmpty.setVisibility(VISIBLE);
                    lvlPic.setVisibility(GONE);
                }

            });


            imagePopup.initiatePopupWithGlide(arrayList.get(position));
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    imagePopup.viewPopup();
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        return currentAPIVersion < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1234);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&  grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.e("OOOn", "Done");
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    public void bottonChoseoption() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.activity_image_select, null);
        mBottomSheetDialog.setContentView(sheetView);

        TextView textViewCamera = sheetView.findViewById(R.id.textViewCamera);
        TextView textViewGallery = sheetView.findViewById(R.id.textViewGallery);
        TextView textViewCancel = sheetView.findViewById(R.id.textViewCancel);


        mBottomSheetDialog.show();

        textViewCamera.setOnClickListener(v -> {

            mBottomSheetDialog.cancel();
            imagePicker.withActivity(UploadPrescriptionActivity.this).chooseFromGallery(false).chooseFromCamera(true).withCompression(true).start();


        });
        textViewGallery.setOnClickListener(v -> {
            mBottomSheetDialog.cancel();
            imagePicker.withActivity(UploadPrescriptionActivity.this).chooseFromGallery(true).chooseFromCamera(false).withCompression(true).start();

        });
        textViewCancel.setOnClickListener(v -> mBottomSheetDialog.cancel());
    }


    public void bottomSheetForAddress() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.address_selection_bottomsheet_layout, null);
        mBottomSheetDialog.setContentView(sheetView);

        TextView tvAddAddress = sheetView.findViewById(R.id.tvAddAddress_AddressSelectionBottomSheet);
        TextView tvClose = sheetView.findViewById(R.id.tvClose_AddressSelectionBottomSheet);
        rvAddresses = sheetView.findViewById(R.id.rvAddresses_AddressSelectionBottomSheet);
        LinearLayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(UploadPrescriptionActivity.this, LinearLayoutManager.VERTICAL, false);
        rvAddresses.setLayoutManager(layoutManager);
        adepterAddress = new AdepterAddress(UploadPrescriptionActivity.this, addresses.getAddressList());
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
                startActivity(new Intent(UploadPrescriptionActivity.this, LocationGetActivity.class)
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


    public void bottonVelidation() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.custome_vallid_layout, null);
        mBottomSheetDialog.setContentView(sheetView);


        mBottomSheetDialog.show();
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


            /*holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if(b){
                        sessionManager.setIntData("position", position);
                        sessionManager.setStringData(pincode, mBanner.get(position).getPincodeId());
                        sessionManager.setStringData(pincoded, mBanner.get(position).getAddress());
                        txtAtype.setText("" + addresses.getAddressList().get(sessionManager.getIntData("position")).getType());
                        txtAddress.setText("" + addresses.getAddressList().get(sessionManager.getIntData("position")).getAddress());
                        sessionManager.setStringData(SessionManager.address, addresses.getAddressList().get(sessionManager.getIntData("position")).getAddress());
                        //notifyDataSetChanged();
                    }

                }
            });*/

            //notifyDataSetChanged();



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