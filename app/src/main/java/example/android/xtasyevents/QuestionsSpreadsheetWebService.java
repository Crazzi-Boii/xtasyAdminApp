package example.android.xtasyevents;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface QuestionsSpreadsheetWebService {

    @POST("1FAIpQLSd2wHHybYT1uw2UekbIxO3cMEue7wZOnQBXBCBhqzgO1nM_nw/formResponse")
    @FormUrlEncoded
    Call<Void> completeQuestionnaire(
            @Field("entry.155335823") String xtasyid,
            @Field("entry.697295605") String name,
            @Field("entry.1430825468") String email,
            @Field("entry.1542108887") String college,
            @Field("entry.1885113587") String contact,
            @Field("entry.1781064420") String gender,
            @Field("entry.232860767") String extras
    );

}