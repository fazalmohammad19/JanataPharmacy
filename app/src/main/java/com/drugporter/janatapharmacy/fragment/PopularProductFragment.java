package com.drugporter.janatapharmacy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drugporter.janatapharmacy.R;
import com.drugporter.janatapharmacy.adepter.ProductAdapter;
import com.drugporter.janatapharmacy.model.BProduct;
import com.drugporter.janatapharmacy.model.Medicine;
import com.drugporter.janatapharmacy.model.User;
import com.drugporter.janatapharmacy.retrofit.APIClient;
import com.drugporter.janatapharmacy.retrofit.GetResult;
import com.drugporter.janatapharmacy.ui.ProductDetailsActivity;
import com.drugporter.janatapharmacy.utiles.CustPrograssbar;
import com.drugporter.janatapharmacy.utiles.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import static com.drugporter.janatapharmacy.utiles.SessionManager.pincode;


public class PopularProductFragment extends Fragment implements ProductAdapter.RecyclerTouchListener, GetResult.MyListener {

    @BindView(R.id.recycler_product)
    RecyclerView recyclerProduct;

    ProductAdapter productAdapter;
    SessionManager sessionManager;
    User user;
    CustPrograssbar custPrograssbar;
    public PopularProductFragment() {
        // Required empty public constructor
    }

    public static PopularProductFragment newInstance(String pincode, String bid) {
        PopularProductFragment fragment = new PopularProductFragment();
        Bundle args = new Bundle();
        args.putString("titel", pincode);
        args.putString("bid", bid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        ButterKnife.bind(this, view);
        custPrograssbar=new CustPrograssbar();
        sessionManager = new SessionManager(getActivity());
        user = sessionManager.getUserDetails("");
        recyclerProduct.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerProduct.setItemAnimator(new DefaultItemAnimator());
        getAllProduct();
        return view;
    }

    private void getAllProduct() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("pincode", sessionManager.getStringData(pincode));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getRandomProduct(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");
    }

    @Override
    public void onClickProductItem(String titel, Medicine medicine) {
        startActivity(new Intent(getActivity(), ProductDetailsActivity.class).putExtra("MyClass", medicine).putParcelableArrayListExtra("PriceList", medicine.getProductInfo()).putStringArrayListExtra("ImageList", medicine.getProductImage()));
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            if (callNo.equalsIgnoreCase("1")) {
                custPrograssbar.closePrograssBar();
                Gson gson = new Gson();
                BProduct product = gson.fromJson(result.toString(), BProduct.class);
                if (product.getResult().equalsIgnoreCase("true")) {
                    productAdapter = new ProductAdapter(getActivity(), product.getResultData(), this);
                    recyclerProduct.setAdapter(productAdapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}