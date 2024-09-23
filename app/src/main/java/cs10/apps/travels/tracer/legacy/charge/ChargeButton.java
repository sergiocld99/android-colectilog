package cs10.apps.travels.tracer.legacy.charge;

import android.view.View;

import cs10.apps.travels.tracer.databinding.ViewCircularButtonBinding;

public class ChargeButton implements View.OnClickListener {
    private final ViewCircularButtonBinding reference;
    private ChargeButtonCallback callback;
    private final int index;

    public ChargeButton(ViewCircularButtonBinding reference, int index){
        this.reference = reference;
        this.index = index;
        this.reference.button.setOnClickListener(this);
    }

    public void setCallback(ChargeButtonCallback callback) {
        this.callback = callback;
    }

    public void setLabel(String text){
        reference.label.setText(text);
    }

    public int getValue(){
        String text = reference.label.getText().toString();
        return Integer.parseInt(text.replace("$",""));
    }

    @Override
    public void onClick(View view) {
        callback.updateChargeSelected(getValue(), index);
        view.setAlpha(1f);
    }

    public void deselect() {
        reference.button.setAlpha(0.6f);
    }

    public void select(){
        reference.button.setAlpha(1f);
    }
}
