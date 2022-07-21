package com.gotaxi.driver.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gotaxi.driver.API.RetrofitClient;
import com.gotaxi.driver.DriverApplication;
import com.gotaxi.driver.Helper.CustomDialog;
import com.gotaxi.driver.Helper.SharedHelper;
import com.gotaxi.driver.Helper.URLHelper;
import com.gotaxi.driver.Models.DocumentResponse;
import com.gotaxi.driver.Models.docResposne;
import com.gotaxi.driver.Models.document;
import com.gotaxi.driver.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadDocumentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<document> documentList;
    private Context context = UploadDocumentActivity.this;
    private Uri selectedimage;
    private String _pid = "", _did = "";
    private ProgressDialog progressDialog;
    private Button uploaded;
    private int STORAGE_PERMISSION = 1;
    private ImageView backArrow;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        uploaded = findViewById(R.id.uploded);
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        uploaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(UploadDocumentActivity.this, WaitingForApproval.class));
                Toast.makeText(context, "Document Uploaded ! Wait some time while our support team will verify your documents.", Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog = new ProgressDialog(UploadDocumentActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.setTitle(R.string.app_name);
        progressDialog.create();
        if (ContextCompat.checkSelfPermission(UploadDocumentActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed for upload Assignment")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(UploadDocumentActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                }, STORAGE_PERMISSION);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                }, STORAGE_PERMISSION);
            }
        }
        getProfile();
        //     Toast.makeText(this, ""+ SharedHelper.getKey(context, "id"), Toast.LENGTH_SHORT).show();


    }

    private void getalldocs() {
        CustomDialog customDialog = new CustomDialog(this);
        customDialog.show();
        Call<DocumentResponse> call = RetrofitClient.getInstance().getApi().getalldocs(_pid);
        call.enqueue(new Callback<DocumentResponse>() {
            @Override
            public void onResponse(Call<DocumentResponse> call, Response<DocumentResponse> response) {
                documentList = response.body().getDocuments();
                documentlistadapter adapter = new documentlistadapter(UploadDocumentActivity.this, documentList);
                recyclerView.setAdapter(adapter);
                customDialog.dismiss();
            }

            @Override
            public void onFailure(Call<DocumentResponse> call, Throwable t) {
                customDialog.dismiss();
                finish();

            }
        });
    }

    public void getProfile() {
        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedHelper.putKey(context, "id", response.optString("id"));
                SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                SharedHelper.putKey(context, "email", response.optString("email"));
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                _pid = response.optString("id");
                getalldocs();
                if (response.optString("avatar").startsWith("http"))
                    SharedHelper.putKey(context, "picture", response.optString("avatar"));
                else
                    SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                SharedHelper.putKey(context, "gender", response.optString("gender"));
                SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                SharedHelper.putKey(context, "approval_status", response.optString("status"));
//                    SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
//                    SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                SharedHelper.putKey(context, "loggedIn", getString(R.string.True));

                if (response.optJSONObject("service") != null) {
                    try {
                        JSONObject service = response.optJSONObject("service");
                        if (service.optJSONObject("service_type") != null) {
                            JSONObject serviceType = service.optJSONObject("service_type");
                            SharedHelper.putKey(context, "service", serviceType.optString("name"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DriverApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            //the image URI
            Uri selectedimages = data.getData();
            try {
                uploadFile(selectedimages);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadFile(Uri fileUri) throws IOException {

        //creating a file
        File file = new File(getRealPathFromURI(fileUri));
        //  File newfile = saveBitmapToFile(file);
        File newfile = new Compressor(this).compressToFile(file);

        //
        //creating request body for file

        MultipartBody.Part pic = null;
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), newfile);
        pic = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        RequestBody pid = RequestBody.create(MediaType.parse("text/plain"), _pid);
        RequestBody did = RequestBody.create(MediaType.parse("text/plain"), _did);


        Call<docResposne> call = RetrofitClient.getInstance().getApi().uploaddoc(pic, pid, did);
        call.enqueue(new Callback<docResposne>() {
            @Override
            public void onResponse(Call<docResposne> call, Response<docResposne> response) {
                Toast.makeText(UploadDocumentActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                getalldocs();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<docResposne> call, Throwable t) {

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public File saveBitmapToFile(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }


    private class documentlistadapter extends RecyclerView.Adapter<documentlistadapter.doclistholder> {
        private Context mxtx;
        private List<document> documentList;

        public documentlistadapter(Context mxtx, List<document> documentList) {
            this.mxtx = mxtx;
            this.documentList = documentList;
        }

        @Override
        public doclistholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mxtx).inflate(R.layout.item_document, parent, false);
            return new documentlistadapter.doclistholder(view);
        }

        @Override
        public void onBindViewHolder(final doclistholder holder, int position) {
            final document documents = documentList.get(position);
            holder.name.setText(documents.getDname());
            holder.type.setText(documents.getDtype());
            holder.upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.show();
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, 100);
                    _did = documents.getDid();

                }

            });

        }


        @Override
        public int getItemCount() {
            return documentList.size();
        }

        class doclistholder extends RecyclerView.ViewHolder {
            TextView name, type, done;
            AppCompatButton upload;

            public doclistholder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.d_name);
                type = itemView.findViewById(R.id.d_type);
                upload = itemView.findViewById(R.id.d_upload);
                done = itemView.findViewById(R.id.d_done);
            }
        }
    }
}
