package com.drugporter.janatapharmacy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.adepter.BannerAdapter;
import com.drugporter.janatapharmacy.adepter.CategoryAdapter;
import com.drugporter.janatapharmacy.adepter.FeatureBrandAdapter;
import com.drugporter.janatapharmacy.adepter.ProductHomeAdapter;
import com.drugporter.janatapharmacy.adepter.UserAdapter;
import com.drugporter.janatapharmacy.model.Banner;
import com.drugporter.janatapharmacy.model.Home;
import com.drugporter.janatapharmacy.model.Medicine;
import com.drugporter.janatapharmacy.model.User;
import com.drugporter.janatapharmacy.retrofit.APIClient;
import com.drugporter.janatapharmacy.retrofit.GetResult;
import com.drugporter.janatapharmacy.ui.HomeActivity;
import com.drugporter.janatapharmacy.ui.ProductDetailsActivity;
import com.drugporter.janatapharmacy.ui.UploadPrescriptionActivity;
import com.drugporter.janatapharmacy.utiles.CustPrograssbar;
import com.drugporter.janatapharmacy.utiles.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import static com.drugporter.janatapharmacy.utiles.SessionManager.about;
import static com.drugporter.janatapharmacy.utiles.SessionManager.contact;
import static com.drugporter.janatapharmacy.utiles.SessionManager.currency;
import static com.drugporter.janatapharmacy.utiles.SessionManager.pincode;
import static com.drugporter.janatapharmacy.utiles.SessionManager.policy;
import static com.drugporter.janatapharmacy.utiles.SessionManager.terms;


public class HomeFragment extends Fragment implements CategoryAdapter.RecyclerTouchListener, FeatureBrandAdapter.RecyclerTouchListener, UserAdapter.RecyclerTouchListener, ProductHomeAdapter.RecyclerTouchListener, GetResult.MyListener {

    @BindView(R.id.my_recycler_view)
    RecyclerView myRecyclerBanner;
    @BindView(R.id.recycler_category)
    RecyclerView recyclerCategory;
    @BindView(R.id.recycler_product)
    RecyclerView recyclerProduct;
    @BindView(R.id.recycler_brand)
    RecyclerView recyclerBrand;
    @BindView(R.id.recycler_user)
    RecyclerView recyclerUser;
    LinearLayoutManager layoutManager;
    int position;
    Timer timer;
    TimerTask timerTask;
    CategoryAdapter categoryAdapter;
    ProductHomeAdapter productAdapter;
    FeatureBrandAdapter featureBrandAdapter;
    UserAdapter userAdapter;
    @BindView(R.id.txt_uprescription)
    TextView txtPrescription;
    @BindView(R.id.img_upload)
    ImageView imgUpload;
    @BindView(R.id.lvl_upload)
    LinearLayout lvlUpload;
    @BindView(R.id.txt_viewll_category)
    TextView txtViewAllCategory;
    @BindView(R.id.lvlv)
    LinearLayout lvlv;
    @BindView(R.id.txt_viewll_product)
    TextView txtViewllProduct;
    @BindView(R.id.lvlv1)
    LinearLayout lvlv1;
    @BindView(R.id.txt_viewll_brand)
    TextView txtViewAllBrand;
    @BindView(R.id.lvlv2)
    LinearLayout lvlTwo;
    @BindView(R.id.txt_viewll_user)
    TextView txtViewllUser;
    @BindView(R.id.lvlv3)
    LinearLayout lvlTree;

    public HomeFragment() {
    }

    SessionManager sessionManager;
    User user;
    BannerAdapter bannerAdapter;
    List<Banner> bannerList = new ArrayList<>();
    CustPrograssbar custPrograssbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        sessionManager = new SessionManager(getActivity());
        user = sessionManager.getUserDetails("");
        custPrograssbar = new CustPrograssbar();
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        myRecyclerBanner.setLayoutManager(layoutManager);
        setbanner();

        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(getActivity());
        mLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerCategory.setLayoutManager(mLayoutManager1);
        recyclerCategory.setItemAnimator(new DefaultItemAnimator());


        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity());
        mLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerProduct.setLayoutManager(mLayoutManager2);
        recyclerProduct.setItemAnimator(new DefaultItemAnimator());


        LinearLayoutManager mLayoutManager3 = new LinearLayoutManager(getActivity());
        mLayoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerBrand.setLayoutManager(mLayoutManager3);
        recyclerBrand.setItemAnimator(new DefaultItemAnimator());


        LinearLayoutManager mLayoutManager4 = new LinearLayoutManager(getActivity());
        mLayoutManager4.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerUser.setLayoutManager(mLayoutManager4);
        recyclerUser.setItemAnimator(new DefaultItemAnimator());

        getHome();

        return view;
    }


    @Override
    public void onClickProductItem(String titel, Medicine medicine) {
        startActivity(new Intent(getActivity(), ProductDetailsActivity.class).putExtra("MyClass", medicine).putParcelableArrayListExtra("PriceList", medicine.getProductInfo()).putStringArrayListExtra("ImageList", medicine.getProductImage()));
    }

    @Override
    public void onClickCategoryItem(String titel, int position) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        Fragment fragment = new SubCategoryFragment();
        fragment.setArguments(args);
        HomeActivity.getInstance().openFragment(fragment);
    }

    @Override
    public void onClickFeaturItem(String titel, String bid) {

        HomeActivity.getInstance().openFragment(new BrandProductFragment().newInstance(titel, bid));
    }

    @Override
    public void onClickUserItem(String titel, int position) {

    }

    @OnClick({R.id.lvl_upload, R.id.txt_viewll_category, R.id.txt_viewll_product, R.id.txt_viewll_brand, R.id.txt_viewll_user})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lvl_upload:
                startActivity(new Intent(getActivity(), UploadPrescriptionActivity.class));
                break;
            case R.id.txt_viewll_category:
                HomeActivity.getInstance().openFragment(new CategoryFragment());
                break;
            case R.id.txt_viewll_product:
                HomeActivity.getInstance().openFragment(new PopularProductFragment());
                break;
            case R.id.txt_viewll_brand:
                HomeActivity.getInstance().openFragment(new BrandFragment());
                break;
            default:
                break;
        }
    }


    private void getHome() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            if(!HomeActivity.userId.equalsIgnoreCase("")){
                jsonObject.put("uid", HomeActivity.userId.toString());
            }else{
                jsonObject.put("uid", user.getId());
            }

            jsonObject.put("pincode", sessionManager.getStringData(pincode));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getHome(bodyRequest);
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
                Home home = gson.fromJson(result.toString(), Home.class);
                if (home.getResult().equalsIgnoreCase("true")) {

                    bannerList = home.getResultData().getBanner();
                    sessionManager.setStringData(currency, home.getResultData().getMainData().getCurrency());
                    sessionManager.setStringData(terms, home.getResultData().getMainData().getTerms());
                    sessionManager.setStringData(contact, home.getResultData().getMainData().getContact());
                    sessionManager.setStringData(about, home.getResultData().getMainData().getAbout());
                    sessionManager.setStringData(policy, home.getResultData().getMainData().getPolicy());

                    bannerAdapter = new BannerAdapter(getActivity(), bannerList);
                    myRecyclerBanner.setAdapter(bannerAdapter);

                    categoryAdapter = new CategoryAdapter(getActivity(), home.getResultData().getCatlist(), this, "single");
                    recyclerCategory.setAdapter(categoryAdapter);

                    productAdapter = new ProductHomeAdapter(getActivity(), home.getResultData().getMedicine(), this);
                    recyclerProduct.setAdapter(productAdapter);

                    featureBrandAdapter = new FeatureBrandAdapter(getActivity(), home.getResultData().getBrand(), this, "single");
                    recyclerBrand.setAdapter(featureBrandAdapter);

                    userAdapter = new UserAdapter(getActivity(), home.getResultData().getTestimonial(), this);
                    recyclerUser.setAdapter(userAdapter);


                } else {
                    Toast.makeText(getActivity(), home.getResponseMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("Error", "-->" + e.toString());
        }
    }


    private void stopAutoScrollBanner() {
        if (timer != null && timerTask != null) {
            timerTask.cancel();
            timer.cancel();
            timer = null;
            timerTask = null;
            position = layoutManager.findFirstCompletelyVisibleItemPosition();
        }
    }

    private void runAutoScrollBanner() {
        if (timer == null && timerTask == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (position == myRecyclerBanner.getAdapter().getItemCount() - 1) {
                            position = 0;
                            myRecyclerBanner.smoothScrollBy(5, 0);
                            myRecyclerBanner.smoothScrollToPosition(position);
                        } else {
                            position++;
                            myRecyclerBanner.smoothScrollToPosition(position);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            };
            timer.schedule(timerTask, 4000, 4000);
        }

    }


    private void setbanner() {
        position = 0;
        myRecyclerBanner.scrollToPosition(position);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(myRecyclerBanner);
        myRecyclerBanner.smoothScrollBy(5, 0);

        myRecyclerBanner.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == 1) {
                    stopAutoScrollBanner();
                } else if (newState == 0) {
                    position = layoutManager.findFirstCompletelyVisibleItemPosition();
                    runAutoScrollBanner();
                }
            }
        });
    }


}