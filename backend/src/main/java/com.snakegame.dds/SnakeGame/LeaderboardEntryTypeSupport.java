package com.snakegame.dds.SnakeGame;

import com.zrdds.infrastructure.*;
import com.zrdds.topic.TypeSupport;
import com.zrdds.publication.DataWriter;
import com.zrdds.subscription.DataReader;
import java.io.UnsupportedEncodingException;

public class LeaderboardEntryTypeSupport extends TypeSupport {
    private String type_name = "LeaderboardEntry";
    private static TypeCodeImpl s_typeCode = null;
    private static LeaderboardEntryTypeSupport m_instance = new LeaderboardEntryTypeSupport();

    private final byte[] tmp_byte_obj = new byte[1];
    private final char[] tmp_char_obj = new char[1];
    private final short[] tmp_short_obj = new short[1];
    private final int[] tmp_int_obj = new int[1];
    private final long[] tmp_long_obj = new long[1];
    private final float[] tmp_float_obj = new float[1];
    private final double[] tmp_double_obj = new double[1];
    private final boolean[] tmp_boolean_obj = new boolean[1];

    
    private LeaderboardEntryTypeSupport(){}

    
    public static TypeSupport get_instance() { return m_instance; }

    public Object create_sampleI() {
        LeaderboardEntry sample = new LeaderboardEntry();
        return sample;
    }

    public void destroy_sampleI(Object sample) {

    }

    public int copy_sampleI(Object dst,Object src) {
        LeaderboardEntry LeaderboardEntryDst = (LeaderboardEntry)dst;
        LeaderboardEntry LeaderboardEntrySrc = (LeaderboardEntry)src;
        LeaderboardEntryDst.copy(LeaderboardEntrySrc);
        return 1;
    }

    public int print_sample(Object _sample) {
        if (_sample == null){
            System.out.println("NULL");
            return -1;
        }
        LeaderboardEntry sample = (LeaderboardEntry)_sample;
        if (sample.player_id != null){
            System.out.println("sample.player_id:" + sample.player_id);
        }
        else{
            System.out.println("sample.player_id: null");
        }
        if (sample.nickname != null){
            System.out.println("sample.nickname:" + sample.nickname);
        }
        else{
            System.out.println("sample.nickname: null");
        }
        System.out.println("sample.score:" + sample.score);
        return 0;
    }

    public String get_type_name(){
        return this.type_name;
    }

    public int get_max_sizeI(){
        return 524;
    }

    public int get_max_key_sizeI(){
        return 524;
    }

    public boolean has_keyI(){
        return false;
    }

    public String get_keyhashI(Object sample, long cdr){
        return "-1";
    }

    public DataReader create_data_reader() {return new LeaderboardEntryDataReader();}

    public DataWriter create_data_writer() {return new LeaderboardEntryDataWriter();}

    public TypeCode get_inner_typecode(){
        TypeCode userTypeCode = get_typecode();
        if (userTypeCode == null) return null;
        return userTypeCode.get_impl();
    }

    public int get_sizeI(Object _sample,long cdr, int offset) throws UnsupportedEncodingException {
        int initialAlignment = offset;
        LeaderboardEntry sample = (LeaderboardEntry)_sample;
        offset += CDRSerializer.get_string_size(sample.player_id == null ? 0 : sample.player_id.getBytes().length, offset);

        offset += CDRSerializer.get_string_size(sample.nickname == null ? 0 : sample.nickname.getBytes().length, offset);

        offset += CDRSerializer.get_untype_size(4, offset);

        return offset - initialAlignment;
    }

    public int serializeI(Object _sample ,long cdr) {
         LeaderboardEntry sample = (LeaderboardEntry) _sample;

        if (!CDRSerializer.put_string(cdr, sample.player_id, sample.player_id == null ? 0 : sample.player_id.length())){
            System.out.println("serialize sample.player_id failed.");
            return -2;
        }

        if (!CDRSerializer.put_string(cdr, sample.nickname, sample.nickname == null ? 0 : sample.nickname.length())){
            System.out.println("serialize sample.nickname failed.");
            return -2;
        }

        if (!CDRSerializer.put_int(cdr, sample.score)){
            System.out.println("serialize sample.score failed.");
            return -2;
        }

        return 0;
    }

    synchronized public int deserializeI(Object _sample, long cdr){
        LeaderboardEntry sample = (LeaderboardEntry) _sample;
        sample.player_id = CDRDeserializer.get_string(cdr);
        if(sample.player_id ==null){
            System.out.println("deserialize member sample.player_id failed.");
            return -3;
        }

        sample.nickname = CDRDeserializer.get_string(cdr);
        if(sample.nickname ==null){
            System.out.println("deserialize member sample.nickname failed.");
            return -3;
        }

        if (!CDRDeserializer.get_int_array(cdr, tmp_int_obj, 1)){
            System.out.println("deserialize sample.score failed.");
            return -2;
        }
        sample.score= tmp_int_obj[0];

        return 0;
    }

    public int get_key_sizeI(Object _sample,long cdr,int offset)throws UnsupportedEncodingException {
        int initialAlignment = offset;
        LeaderboardEntry sample = (LeaderboardEntry)_sample;
        offset += get_sizeI(sample, cdr, offset);
        return offset - initialAlignment;
    }

    public int serialize_keyI(Object _sample, long cdr){
        LeaderboardEntry sample = (LeaderboardEntry)_sample;
        return 0;
    }

    public int deserialize_keyI(Object _sample, long cdr) {
        LeaderboardEntry sample = (LeaderboardEntry)_sample;
        return 0;
    }

    public TypeCode get_typecode(){
        if (s_typeCode != null) {
            return s_typeCode;
        }
        TypeCodeFactory factory = TypeCodeFactory.get_instance();

        s_typeCode = factory.create_struct_TC("SnakeGame.LeaderboardEntry");
        if (s_typeCode == null){
            System.out.println("create struct LeaderboardEntry typecode failed.");
            return s_typeCode;
        }
        int ret = 0;
        TypeCodeImpl memberTc = new TypeCodeImpl();
        TypeCodeImpl eleTc = new TypeCodeImpl();

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member player_id TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            0,
            0,
            "player_id",
            memberTc,
            false,
            false);
        factory.delete_TC(memberTc);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member nickname TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            1,
            1,
            "nickname",
            memberTc,
            false,
            false);
        factory.delete_TC(memberTc);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        memberTc = factory.get_primitive_TC(TypeCodeKind.DDS_TK_INT);
        if (memberTc == null){
            System.out.println("Get Member score TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            2,
            2,
            "score",
            memberTc,
            false,
            false);
        if (ret < 0)
        {
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }

        return s_typeCode;
    }

}