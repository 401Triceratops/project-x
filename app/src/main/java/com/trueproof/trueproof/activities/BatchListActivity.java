package com.trueproof.trueproof.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Batch;
import com.trueproof.trueproof.R;
import com.trueproof.trueproof.adapters.ActiveBatchListAdapter;
import com.trueproof.trueproof.adapters.BatchListAdapter;
import com.trueproof.trueproof.models.BatchUtils;
import com.trueproof.trueproof.utils.JsonConverter;
import com.trueproof.trueproof.viewmodels.BatchListViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BatchListActivity extends AppCompatActivity {
    static final private String TAG = "BatchListActivity";
    static final int REDIRECT_TO_BATCH_DETAIL_TO_TAKE_MEASUREMENT = 1;
    private BatchListViewModel viewModel;
    private BatchListAdapter batchListAdapter;
    private ActiveBatchListAdapter activeBatchListAdapter;

    ActivityResultLauncher<Intent> newBatchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::onActivityResult
    );

    @Inject
    JsonConverter jsonConverter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_list);
        viewModel = new ViewModelProvider(this).get(BatchListViewModel.class);

        setUpAllBatchList();
        setUpActiveBatchList();

        findViewById(R.id.imageButtonNewBatchBatchList).setOnClickListener(
                v -> goToNewBatchActivity()
        );
    }

    private void setUpAllBatchList() {
        RecyclerView allBatchList = findViewById(R.id.recyclerViewBatchList);
        allBatchList.setLayoutManager(new LinearLayoutManager(this));
        batchListAdapter = new BatchListAdapter(batch -> {
            // TODO: Go to the batch detail
            Log.i(TAG, "BatchList: clicked on batch " + BatchUtils.batchToString(batch));
            goToBatchDetail(batch);
        });

        final List<Batch> list = viewModel.getBatchList().getValue();
        if (list != null) batchListAdapter.submitList(list);
        else batchListAdapter.submitList(new ArrayList<Batch>());

        allBatchList.setLayoutManager(new LinearLayoutManager(this));
        allBatchList.setAdapter(batchListAdapter);
        viewModel.getBatchList().observe(this,
                batches -> batchListAdapter.submitList(batches));
    }

    private void setUpActiveBatchList() {
        RecyclerView activeBatchList = findViewById(R.id.recyclerViewActiveBatchList);
        activeBatchList.setLayoutManager(new LinearLayoutManager(this));
        activeBatchListAdapter = new ActiveBatchListAdapter(batch -> {
            Log.i(TAG, "ActiveBatchList: clicked on batch " + BatchUtils.batchToString(batch));
            goToBatchDetail(batch);
        });
        final List<Batch> list = viewModel.getActiveBatchList().getValue();
        if (list != null) activeBatchListAdapter.submitList(list);
        else activeBatchListAdapter.submitList(new ArrayList<Batch>());

        activeBatchList.setAdapter(activeBatchListAdapter);
        viewModel.getActiveBatchList().observe(this, batches ->
                activeBatchListAdapter.submitList(batches));
    }

    private void goToBatchDetail(Batch batch) {
        Intent intent = new Intent(this, BatchDetailActivity.class);
        intent.putExtra(BatchDetailActivity.BATCH_JSON, jsonConverter.batchToJson(batch));
    }

    private void goToNewBatchActivity() {
        Intent intent = new Intent(this, NewBatchActivity.class);
        newBatchLauncher.launch(intent);
    }

    private void onActivityResult(ActivityResult activityResult) {
        Log.i(TAG, "onActivityResult: ");
        Log.i(TAG, "getResultCode(): " + activityResult.getResultCode());
        if (activityResult.getResultCode() == REDIRECT_TO_BATCH_DETAIL_TO_TAKE_MEASUREMENT) {
            String json = activityResult.getData().getStringExtra(BatchDetailActivity.BATCH_JSON);
            Intent batchDetailIntent = new Intent(this, BatchDetailActivity.class);
            batchDetailIntent.putExtra(BatchDetailActivity.REDIRECT_TO_TAKE_MEASUREMENT, true);
            batchDetailIntent.putExtra(BatchDetailActivity.BATCH_JSON, json);
            startActivity(batchDetailIntent);
        }
}
}