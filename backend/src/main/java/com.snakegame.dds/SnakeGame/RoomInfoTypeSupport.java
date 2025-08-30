package com.snakegame.dds.SnakeGame;

import com.zrdds.infrastructure.*;
import com.zrdds.topic.TypeSupport;
import com.zrdds.publication.DataWriter;
import com.zrdds.subscription.DataReader;
import java.io.UnsupportedEncodingException;

public class RoomInfoTypeSupport extends TypeSupport {
    private String type_name = "RoomInfo";
    private static TypeCodeImpl s_typeCode = null;
    private static RoomInfoTypeSupport m_instance = new RoomInfoTypeSupport();

    private final byte[] tmp_byte_obj = new byte[1];
    private final char[] tmp_char_obj = new char[1];
    private final short[] tmp_short_obj = new short[1];
    private final int[] tmp_int_obj = new int[1];
    private final long[] tmp_long_obj = new long[1];
    private final float[] tmp_float_obj = new float[1];
    private final double[] tmp_double_obj = new double[1];
    private final boolean[] tmp_boolean_obj = new boolean[1];

    
    private RoomInfoTypeSupport(){}

    
    public static TypeSupport get_instance() { return m_instance; }

    public Object create_sampleI() {
        RoomInfo sample = new RoomInfo();
        return sample;
    }

    public void destroy_sampleI(Object sample) {

    }

    public int copy_sampleI(Object dst,Object src) {
        RoomInfo RoomInfoDst = (RoomInfo)dst;
        RoomInfo RoomInfoSrc = (RoomInfo)src;
        RoomInfoDst.copy(RoomInfoSrc);
        return 1;
    }

    public int print_sample(Object _sample) {
        if (_sample == null){
            System.out.println("NULL");
            return -1;
        }
        RoomInfo sample = (RoomInfo)_sample;
        if (sample.room_id != null){
            System.out.println("sample.room_id:" + sample.room_id);
        }
        else{
            System.out.println("sample.room_id: null");
        }
        int player_idsTmpLen = sample.player_ids.length();
        System.out.println("sample.player_ids.length():" +player_idsTmpLen);
        for (int i = 0; i < player_idsTmpLen; ++i){
            if (sample.player_ids.get_at(i) != null){
                System.out.println("sample.player_ids.get_at(" + i + "):" + sample.player_ids.get_at(i));
            }
            else{
                System.out.println("sample.player_ids.get_at(" + i + "): null");
            }
        }
        return 0;
    }

    public String get_type_name(){
        return this.type_name;
    }

    public int get_max_sizeI(){
        return 66564;
    }

    public int get_max_key_sizeI(){
        return 66564;
    }

    public boolean has_keyI(){
        return false;
    }

    public String get_keyhashI(Object sample, long cdr){
        return "-1";
    }

    public DataReader create_data_reader() {return new RoomInfoDataReader();}

    public DataWriter create_data_writer() {return new RoomInfoDataWriter();}

    public TypeCode get_inner_typecode(){
        TypeCode userTypeCode = get_typecode();
        if (userTypeCode == null) return null;
        return userTypeCode.get_impl();
    }

    public int get_sizeI(Object _sample,long cdr, int offset) throws UnsupportedEncodingException {
        int initialAlignment = offset;
        RoomInfo sample = (RoomInfo)_sample;
        offset += CDRSerializer.get_string_size(sample.room_id == null ? 0 : sample.room_id.getBytes().length, offset);

        offset += CDRSerializer.get_untype_size(4, offset);
        int player_idsLen = sample.player_ids.length();
        if (player_idsLen != 0){
            for(int i = 0; i<sample.player_ids.length(); ++i)
            {
                offset += CDRSerializer.get_string_size(sample.player_ids.get_at(i).getBytes().length,offset);
            }
        }

        return offset - initialAlignment;
    }

    public int serializeI(Object _sample ,long cdr) {
         RoomInfo sample = (RoomInfo) _sample;

        if (!CDRSerializer.put_string(cdr, sample.room_id, sample.room_id == null ? 0 : sample.room_id.length())){
            System.out.println("serialize sample.room_id failed.");
            return -2;
        }

        if (!CDRSerializer.put_int(cdr, sample.player_ids.length())){
            System.out.println("serialize length of sample.player_ids failed.");
            return -2;
        }
        for (int i = 0; i < sample.player_ids.length(); ++i){
            if (!CDRSerializer.put_string(cdr, sample.player_ids.get_at(i), sample.player_ids.get_at(i) == null ? 0 : sample.player_ids.get_at(i).length())){
                System.out.println("serialize sample.player_ids failed.");
                return -2;
            }
        }

        return 0;
    }

    synchronized public int deserializeI(Object _sample, long cdr){
        RoomInfo sample = (RoomInfo) _sample;
        sample.room_id = CDRDeserializer.get_string(cdr);
        if(sample.room_id ==null){
            System.out.println("deserialize member sample.room_id failed.");
            return -3;
        }

        if (!CDRDeserializer.get_int_array(cdr, tmp_int_obj, 1)){
            System.out.println("deserialize length of sample.player_ids failed.");
            return -2;
        }
        if (!sample.player_ids.ensure_length(tmp_int_obj[0], tmp_int_obj[0])){
            System.out.println("Set maxiumum member sample.player_ids failed.");
            return -3;
        }
        for(int i =0 ;i < sample.player_ids.length() ;++i)
        {
            sample.player_ids.set_at(i, CDRDeserializer.get_string(cdr));
        }

        return 0;
    }

    public int get_key_sizeI(Object _sample,long cdr,int offset)throws UnsupportedEncodingException {
        int initialAlignment = offset;
        RoomInfo sample = (RoomInfo)_sample;
        offset += get_sizeI(sample, cdr, offset);
        return offset - initialAlignment;
    }

    public int serialize_keyI(Object _sample, long cdr){
        RoomInfo sample = (RoomInfo)_sample;
        return 0;
    }

    public int deserialize_keyI(Object _sample, long cdr) {
        RoomInfo sample = (RoomInfo)_sample;
        return 0;
    }

    public TypeCode get_typecode(){
        if (s_typeCode != null) {
            return s_typeCode;
        }
        TypeCodeFactory factory = TypeCodeFactory.get_instance();

        s_typeCode = factory.create_struct_TC("SnakeGame.RoomInfo");
        if (s_typeCode == null){
            System.out.println("create struct RoomInfo typecode failed.");
            return s_typeCode;
        }
        int ret = 0;
        TypeCodeImpl memberTc = new TypeCodeImpl();
        TypeCodeImpl eleTc = new TypeCodeImpl();

        memberTc = factory.create_string_TC(255);
        if (memberTc == null){
            System.out.println("Get Member room_id TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            0,
            0,
            "room_id",
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
        if (memberTc != null)
        {
            memberTc = factory.create_sequence_TC(255, memberTc);
        }
        if (memberTc == null){
            System.out.println("Get Member player_ids TypeCode failed.");
            factory.delete_TC(s_typeCode);
            s_typeCode = null;
            return null;
        }
        ret = s_typeCode.add_member_to_struct(
            1,
            1,
            "player_ids",
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

        return s_typeCode;
    }

}