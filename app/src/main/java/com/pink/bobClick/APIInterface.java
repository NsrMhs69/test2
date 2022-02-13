package com.pink.bobClick;




import com.pink.bobClick.model.InputApplicationVersionDTO;
import com.pink.bobClick.model.OutputGetApplicationVersion;
import com.pink.bobClick.model.OutputServiceModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIInterface {

    @POST("GetApplicationVersion")
    Call<OutputServiceModel<OutputGetApplicationVersion>> getApplicationVersionCode(@Body InputApplicationVersionDTO inputApplicationVersionDTO);

}

