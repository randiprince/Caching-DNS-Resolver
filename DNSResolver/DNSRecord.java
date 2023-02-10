import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

public class DNSRecord {
    String[] name_;
    int type_;
    int class_;
    int ttl_;
    int rdlength_;
    byte[] rdata_;
    Calendar date_;

    static DNSRecord decodeRecord(ByteArrayInputStream input, DNSMessage message) throws IOException {
        DNSRecord record = new DNSRecord();
        record.name_ = message.readDomainName(input);
        byte[] type = input.readNBytes(2);
        record.type_ = ((type[0] & 0xff) << 8) | (type[1] & 0xff);
        byte[] rClass = input.readNBytes(2);
        record.class_ = ((rClass[0] & 0xff) << 8) | (rClass[1] & 0xff);
        byte[] ttl = input.readNBytes(4);
        record.ttl_ = ((ttl[0] & 0xff) << 24) | ((ttl[1] & 0xff) << 16) | ((ttl[2] & 0xff) << 8) | (ttl[3] & 0xff);
        byte[] rdlength = input.readNBytes(2);
        record.rdlength_ = ((rdlength[0] & 0xff) << 8) | (rdlength[1] & 0xff);
        record.rdata_ = input.readNBytes(record.rdlength_);

        record.date_ = Calendar.getInstance();
        return record;
    }

    public boolean isExpired() {
        Calendar now = Calendar.getInstance();
        date_.add(Calendar.SECOND, ttl_); // add ttl to record creation date
        return now.before(date_); // return if right now is before or after ttl
    }

    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations) throws IOException {
        DNSMessage.writeDomainName(outputStream, domainLocations, name_); //write name
        //write type
        int byte1 = (type_ >> 8) & 0xff;
        outputStream.write(byte1);
        int byte2 = type_ & 0xff;
        outputStream.write(byte2);

        //write class
        int byte3 = (class_ >> 8) & 0xff;
        outputStream.write(byte3);
        int byte4 = class_ & 0xff;
        outputStream.write(byte4);
        //write ttl
        int byte5 = (ttl_ >> 24) & 0xff;
        outputStream.write(byte5);
        int byte6 = (ttl_ >> 16) & 0xff;
        outputStream.write(byte6);
        int byte7 = (ttl_ >> 8) & 0xff;
        outputStream.write(byte7);
        int byte8 = ttl_ & 0xff;
        outputStream.write(byte8);

        //write rdlength
        int byte9 = (rdlength_ >> 8) & 0xff;
        outputStream.write(byte9);
        int byte10 = rdlength_ & 0xff;
        outputStream.write(byte10);
        // rdata
        outputStream.write(rdata_);

    }
}
