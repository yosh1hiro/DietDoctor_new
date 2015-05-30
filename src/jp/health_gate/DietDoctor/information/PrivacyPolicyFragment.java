package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * プライバシーポリシーを表示するフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class PrivacyPolicyFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.privacy_policy_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("プライバシーポリシー");


        WebView webView = (WebView) getView().findViewById(R.id.web_view);
        webView.loadUrl("file:///android_asset/privacy_policy.html");
    }
}
