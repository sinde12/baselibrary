package hohistar.sinde.baselibrary.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class STFragment extends BaseFragment {

    public STFragment() {}

    public  void changeNavigationView(NavigationView nv){}

    protected  void onAction(){}

    protected boolean onBack(){
        return false;
    }

    protected void onRefresh(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof NavigationActivity) {
            if (savedInstanceState == null){
                NavigationActivity ac = (NavigationActivity) getActivity();
                if (ac.getNavigationView() != null)
                    ac.changeNavigationView(ac.getNavigationView());
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() instanceof NavigationActivity) {
            NavigationActivity ac = (NavigationActivity) getActivity();
            if (savedInstanceState != null){
                ac.changeNavigationView(ac.getNavigationView());
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public NavigationView getNavigationView(){
        if (getActivity() instanceof NavigationActivity){
            NavigationActivity ac = (NavigationActivity) getActivity();
            return ac.getNavigationView();
        }
        return null;
    }

}
