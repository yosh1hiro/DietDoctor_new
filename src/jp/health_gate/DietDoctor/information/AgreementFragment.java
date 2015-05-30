package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * 利用規約を表示するフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class AgreementFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.agreement_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("利用規約");

        WebView webView = (WebView) getView().findViewById(R.id.web_view);
        webView.loadUrl("file:///android_asset/terms_of_service.html");
    }
}
