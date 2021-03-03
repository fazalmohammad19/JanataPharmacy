package com.drugporter.janatapharmacy.ui;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.model.PrescriptionOrderH;
import com.drugporter.janatapharmacy.model.User;
import com.drugporter.janatapharmacy.retrofit.APIClient;
import com.drugporter.janatapharmacy.retrofit.GetResult;
import com.drugporter.janatapharmacy.utiles.CustPrograssbar;
import com.drugporter.janatapharmacy.utiles.SessionManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class PrescriptionOrderDetailsActivity extends RootActivity implements GetResult.MyListener {


    StaggeredGridLayoutManager gridLayoutManager;
    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    User user;
    String oid;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.txt_aditionalinfo)
    TextView txtAditionalinfo;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_actiontitle)
    TextView txtActiontitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.txt_summary)
    TextView txtSummary;
    @BindView(R.id.txt_item)
    TextView txtItem;
    @BindView(R.id.txt_orderid)
    TextView txtOrderid;
    @BindView(R.id.txt_orderstatus)
    TextView txtOrderstatus;
    @BindView(R.id.txt_orderdate)
    TextView txtOrderdate;
    @BindView(R.id.txt_orderddate)
    TextView txtOrderddate;
    @BindView(R.id.scv_summry)
    ScrollView scvSummry;
    @BindView(R.id.my_recycler_view)
    RecyclerView myRecyclerView;
    @BindView(R.id.scv_item)
    ScrollView scvItem;
    @BindView(R.id.lvl_additional)
    LinearLayout lvlAdditional;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescriptionorder_details);
        ButterKnife.bind(this);
        oid = getIntent().getStringExtra("oid");
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(PrescriptionOrderDetailsActivity.this);
        user = sessionManager.getUserDetails("");
        gridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        myRecyclerView.setLayoutManager(gridLayoutManager);
        getOrderHistiry();
    }

    private void getOrderHistiry() {
        custPrograssbar.prograssCreate(PrescriptionOrderDetailsActivity.this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("order_id", oid);
        } catch (Exception e) {
            e.printStackTrace();

        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getPrescriptionOrderHistry(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    @Override
    public void callback(JsonObject result, String callNo) {

        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                PrescriptionOrderH orderP = gson.fromJson(result.toString(), PrescriptionOrderH.class);
                if (orderP.getResult().equalsIgnoreCase("true")) {
                    txtOrderid.setText("" + oid);
                    txtOrderstatus.setText("" + orderP.getPrescriptionOrderProductList().get(0).getOrderStatus());
                    txtOrderdate.setText("" + orderP.getPrescriptionOrderProductList().get(0).getOrderDate());
                    txtAddress.setText("" + orderP.getPrescriptionOrderProductList().get(0).getCustomerAddress());

                    ItemAdp itemAdp = new ItemAdp(PrescriptionOrderDetailsActivity.this, orderP.getPrescriptionOrderProductList().get(0).getPrescriptionImageList());
                    myRecyclerView.setAdapter(itemAdp);
                }
            }
        } catch (Exception e) {
            Log.e("Error", "-->" + e.toString());
        }

    }

    @OnClick({R.id.img_back, R.id.txt_summary, R.id.txt_item})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.txt_summary:
                txtSummary.setTextColor(getResources().getColor(R.color.white));
                txtItem.setTextColor(getResources().getColor(R.color.black));
                txtSummary.setBackground(getResources().getDrawable(R.drawable.orderbox));
                txtItem.setBackground(getResources().getDrawable(R.drawable.orderbox_white));
                scvSummry.setVisibility(View.VISIBLE);
                scvItem.setVisibility(View.GONE);
                break;
            case R.id.txt_item:
                txtSummary.setTextColor(getResources().getColor(R.color.black));
                txtItem.setTextColor(getResources().getColor(R.color.white));
                txtSummary.setBackground(getResources().getDrawable(R.drawable.orderbox_white));
                txtItem.setBackground(getResources().getDrawable(R.drawable.orderbox));
                scvSummry.setVisibility(View.GONE);
                scvItem.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    public class ItemAdp extends RecyclerView.Adapter<ItemAdp.ViewHolder> {

        private List<String> mData;
        private LayoutInflater mInflater;
        Context mContext;


        public ItemAdp(Context context, List<String> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            this.mContext = context;
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.custome_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int i) {
            ImagePopup imagePopup = new ImagePopup(PrescriptionOrderDetailsActivity.this);
            imagePopup.setFullScreen(true);
            // Optional
            imagePopup.setHideCloseIcon(false);
            // Optional
            imagePopup.setImageOnClickClose(false);
            // Optional
            Glide.with(mContext).load(APIClient.baseUrl + "/janata/" + mData.get(i)).thumbnail(Glide.with(mContext).load(R.drawable.ezgifresize)).into(holder.imgIcon);
            imagePopup.initiatePopupWithGlide(APIClient.baseUrl + "/janata/" + mData.get(i));
            holder.imgIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imagePopup.viewPopup();
                }
            });


        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.img_icon)
            ImageView imgIcon;


            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }


    }

}