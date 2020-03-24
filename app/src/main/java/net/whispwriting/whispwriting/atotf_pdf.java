package net.whispwriting.whispwriting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;

import java.io.File;

public class atotf_pdf extends literature {




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pdf);

        File file = new File("assets/atotf_preview") ;
        Uri targetUri = FileProvider.getUriForFile(
                atotf_pdf.this,
                "net.whispwriting.whispwriting.provider", //(use your app signature + ".provider" )
                file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(targetUri, "application/PDF");
        startActivity(intent);

    }
}