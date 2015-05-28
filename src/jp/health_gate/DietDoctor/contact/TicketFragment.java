package jp.health_gate.DietDoctor.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * チケット購入用のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class TicketFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ticket_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.ticket_title);
    }
}
