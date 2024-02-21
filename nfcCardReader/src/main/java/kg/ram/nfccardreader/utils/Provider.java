package kg.ram.nfccardreader.utils;

import android.nfc.tech.IsoDep;
import android.util.Log;

import kg.ram.nfccardreader.BuildConfig;

import kg.ram.nfccardreader.parser.IProvider;

import java.io.IOException;

public class Provider implements IProvider {

    private IsoDep tagCom;

    public void setTagCom(final IsoDep tagCom) {
        this.tagCom = tagCom;
    }

    @Override
    public byte[] transceive(byte[] pCommand) {

        byte[] response = null;
        try {
            // send command to emv card
            response = tagCom.transceive(pCommand);
        } catch (IOException e) {
            if (BuildConfig.LOG_DEBUG_MODE) {
                Log.d("Provider IOException", e.getMessage());
            }
        }
        return response;
    }
}
