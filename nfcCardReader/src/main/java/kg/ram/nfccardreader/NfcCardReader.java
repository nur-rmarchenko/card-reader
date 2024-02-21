package kg.ram.nfccardreader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import kg.ram.nfccardreader.model.EmvCard;
import kg.ram.nfccardreader.parser.EmvParser;
import kg.ram.nfccardreader.utils.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class NfcCardReader {

    private final static String NFC_ISO_DEP = "android.nfc.tech.IsoDep";
    private final static String NFC_A_TAG = "android.nfc.tech.NfcA";
    private final static String NFC_B_TAG = "android.nfc.tech.NfcB";

    private boolean isException;

    public NfcCardResponse readCard(Tag tag) {
        if (tag != null) {
            NfcCardResponse nfcCardResponse = null;
            if (isValidTag(tag)) {
                try {
                    nfcCardResponse = getCardInfo(tag);
                } catch (Exception e) {
                    Log.e(NfcCardReader.class.getName(), e.getMessage(), e);
                }

                if (!isException) {
                    if (nfcCardResponse != null && nfcCardResponse.getEmvCard() != null) {
                        EmvCard emvCard = nfcCardResponse.getEmvCard();
                        if (StringUtils.isNotBlank(emvCard.getCardNumber())) {
                            return NfcCardResponse.createResponse(emvCard);
                        } else if (emvCard.isNfcLocked()) {
                            return NfcCardResponse.createError(NfcCardError.CARD_LOCKED_WITH_NFC);
                        }
                    } else {
                        return NfcCardResponse.createError(NfcCardError.UNKNOWN_EMV_CARD);
                    }
                } else {
                    return NfcCardResponse.createError(NfcCardError.DONOT_MOVE_CARD_SO_FAST);
                }
            } else {
                return NfcCardResponse.createError(NfcCardError.UNKNOWN_EMV_CARD);
            }
        }
        return null;
    }

    private boolean isValidTag(Tag tag) {
        return tag.toString().contains(NFC_ISO_DEP)
                && (tag.toString().contains(NFC_A_TAG) || tag.toString().contains(NFC_B_TAG));
    }

    private NfcCardResponse getCardInfo(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        Provider provider = new Provider();
        if (isoDep == null) {
            return NfcCardResponse.createError(NfcCardError.DONOT_MOVE_CARD_SO_FAST);
        }

        isException = false;

        try {
            // Open connection
            isoDep.connect();

            provider.setTagCom(isoDep);

            EmvParser parser = new EmvParser(provider, true);
            EmvCard card = parser.readEmvCard();
            if (card != null) {
                return NfcCardResponse.createResponse(card);
            }
        } catch (IOException e) {
            isException = true;
            Log.e(NfcCardReader.class.getName(), e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(isoDep);
        }
        return NfcCardResponse.createError(NfcCardError.UNKNOWN_EMV_CARD);
    }
}