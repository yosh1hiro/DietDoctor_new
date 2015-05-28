package jp.health_gate.DietDoctor.models;

import android.util.Log;
import com.kinvey.android.callback.KinveyListCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ランキング読み込み用クラス
 *
 * Created by kazhida on 2013/12/16.
 */
public class RankPoints {

    public interface Callback {
        void onLoaded();
        void onFailed();
    }

    public class Item {
        private float lossRate;
        private float bmi;
        private int starCount;
        private int medalCount;

        public float getLossRate() {
            return lossRate;
        }

        public float getBMI() {
            return bmi;
        }

        public int getStarCount() {
            return starCount;
        }

        @SuppressWarnings("unused") //今は使わないけど、将来のためにサーバ側では用意している
        public int getMedalCount() {
            return medalCount;
        }
    }

    private List<Item> items = new ArrayList<Item>();

    public RankPoints(final Callback callback) {
        Backend.sharedInstance().loadRanking(new KinveyListCallback<Backend.RankPointRecord>() {
            @Override
            public void onSuccess(Backend.RankPointRecord[] rankPointRecords) {
                for (Backend.RankPointRecord record: rankPointRecords) {
                    Item item = new Item();
                    item.lossRate   = (float) record.lossRate;
                    item.bmi        = (float) record.bmi;
                    item.starCount  = record.starCount;
                    item.medalCount = record.medalCount;
                    items.add(item);
                }
                callback.onLoaded();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("DietDoctor", "RankPoints load error: " + throwable == null ? "" : throwable.getLocalizedMessage());
                callback.onFailed();
            }
        });
    }

    public List<Item> getItems() {
        return items;
    }
}
