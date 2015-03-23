/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.json;

import java.math.BigDecimal;

import io.advantageous.boon.Lists;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.json.serializers.impl.JsonSimpleSerializerImpl;
import io.advantageous.boon.json.test.FooEnum;
import io.advantageous.boon.json.test.AllTypes;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.*;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import io.advantageous.boon.core.Dates;

/**
 * Created by rick on 12/18/13.
 */
public class JsonSerializeTest {
    boolean ok;

    public static class Employee {

        String name = "Rick";
        String url = "http://foo.bar/foo.jpg";
        Date dob = Dates.getUSDate( 5, 25, 1980 );
        Currency currency = Currency.getInstance("USD");
        BigDecimal salary = new BigDecimal("100000.00");

        public String getName() {
            return name;
        }

        public void setName( String name ) {
            this.name = name;
        }
    }


    @Test
    public void test() {
        Employee rick = new Employee();
        String sRick = new JsonSimpleSerializerImpl().serialize( rick ).toString();

        puts(sRick);
        ok = sRick.equals( "{\"name\":\"Rick\",\"url\":\"http://foo.bar/foo.jpg\",\"dob\":328147200000,\"currency\":\"USD\",\"salary\":100000.00}" ) || die( sRick );

        rick = new JsonParserFactory().create ().parse ( Employee.class, sRick );
        ok = rick.url.equals( "http://foo.bar/foo.jpg" ) || die( sRick );

    }


    @Test
    public void testWithUTF8() {
        Employee aÉddié = new Employee();
        aÉddié.name = "Éddié";
        String sRick = new JsonSimpleSerializerImpl().serialize( aÉddié ).toString();

        puts(sRick);
        ok = sRick.equals( "{\"name\":\"Éddié\",\"url\":\"http://foo.bar/foo.jpg\",\"dob\":328147200000,\"currency\":\"USD\",\"salary\":100000.00}" ) || die( sRick );

        Employee bÉddié = new JsonParserFactory ().create ().parse ( Employee.class, sRick );

        bÉddié.name.equals(aÉddié.name);
    }

    @Test
    public void testWithAcii() {
        Employee aÉddié = new Employee();
        aÉddié.name = "Éddié";
        String sRick = new JsonSerializerFactory().asciiOnly().create().serialize(aÉddié).toString();

        puts(sRick);
        ok = sRick.equals( "{\"name\":\"\\u00c9ddi\\u00e9\",\"url\":\"http://foo.bar/foo.jpg\",\"dob\":328147200000,\"currency\":\"USD\",\"salary\":100000.00}" ) || die( sRick );

        Employee bÉddié = new JsonParserFactory ().create ().parse ( Employee.class, sRick );

        bÉddié.name.equals(aÉddié.name);
    }

    @Test
    public void testWithType() {


        Employee rick = new Employee();
        String sRick = new JsonSerializerFactory().useFieldsFirst().setOutputType( true ).create()
                .serialize( rick ).toString();

        puts (sRick);
        boolean ok = sRick.equals( "{\"class\":\"io.advantageous.boon.json.JsonSerializeTest$Employee\",\"name\":\"Rick\",\"url\":\"http://foo.bar/foo.jpg\",\"dob\":328147200000,\"currency\":\"USD\",\"salary\":100000.00}" ) || die( sRick );
    }

    @Test
    public void testControlCharEncoding() {


        Employee rick = new Employee();
        rick.setName("\u001d\u001eRick");
        String sRick = new JsonSerializerFactory().useFieldsFirst().setOutputType( true ).create()
                .serialize( rick ).toString();

        puts (sRick);

        ok |= sRick.contains("name\":\"\\u001d\\u001eRick");
    }

    @Test
    public void testBug() {
        Employee rick = new Employee();

        JsonSerializer serializer = new JsonSerializerFactory()
                .setIncludeEmpty( true ).setUseAnnotations( false )
                .setCacheInstances( false )
                .setIncludeNulls( true )
                .create();

        String sRick = serializer.serialize( rick ).toString();

        //uts( sRick );

        AllTypes foo = new AllTypes();
        foo.ignoreMe = "THIS WILL NOT PASS";
        foo.ignoreMe2 = "THIS WILL NOT PASS EITHER";
        foo.ignoreMe3 = "THIS WILL NOT PASS TOO";

        foo.setDate( new Date() );
        foo.setBar( FooEnum.BAR );
        foo.setFoo( FooEnum.FOO );
        foo.setString( "Hi Mom" );
        AllTypes foo2 = BeanUtils.copy( foo );
        foo.setAllType( foo2 );
        foo2.setString( "Hi Dad" );
        foo.setAllTypeList( Lists.list( BeanUtils.copy( foo2 ), BeanUtils.copy( foo2 ) ) );


        String sFoo = serializer.serialize( foo ).toString();
        //uts( sFoo );
    }


    @Test
    public void bug2() {

        Employee rick = new Employee();

        JsonSerializer serializer = new JsonSerializerFactory()
                .setIncludeEmpty( true ).setUseAnnotations( false )
                .setCacheInstances( false )
                .create();

        String sRick = serializer.serialize( rick ).toString();

        outputs( sRick );

        AllTypes foo = new AllTypes();
        foo.ignoreMe = "THIS WILL NOT PASS";
        foo.ignoreMe2 = "THIS WILL NOT PASS EITHER";
        foo.ignoreMe3 = "THIS WILL NOT PASS TOO";

        foo.setDate( new Date() );
        foo.setBar( FooEnum.BAR );
        foo.setFoo( FooEnum.FOO );
        foo.setString( "Hi Mom" );
        AllTypes foo2 = BeanUtils.copy( foo );
        foo.setAllType( foo2 );
        foo2.setString( "Hi Dad" );
        foo.setAllTypeList( Lists.list( BeanUtils.copy( foo2 ), BeanUtils.copy( foo2 ) ) );


        String sFoo = serializer.serialize( foo ).toString();
        outputs( sFoo );

    }

    private void outputs( Object... args ) {
    }

    @Test
    public void bug3() {

        JsonSerializer serializer = new JsonSerializerFactory().create ();

        String sMedium = serializer.serialize( MEDIUM_DATA ).toString();
        outputs( sMedium );

        Map<String, Object> map = new JsonParserFactory ().create ().parseMap ( sMedium );
        map = ( Map<String, Object> ) map.get("photo");
        outputs( "url", map.get( "url" ) );

        String str = serializer.serialize( COMPLEX_DATA ).toString();
        outputs( str );

    }


    private static final Map<String, Object> MEDIUM_DATA = generateMediumData();

    private static Map<String, Object> generateMediumData() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put( "id", "af1c72e67e9c4" );
            data.put( "displayName", "Jonh Smith" );
            data.put( "email", "jonth.smith@mail.net" );
            data.put( "gender", 1 );
            data.put( "birthday", "1985-07-20" );
            data.put( "languages", Arrays.asList( "en", "ru", "fr" ) );
            Map<String, Object> photo = new LinkedHashMap<>();
            photo.put( "width", 160 );
            photo.put( "height", 200 );
            photo.put( "url", new URL( "http://somecdn.local/medium_af1c72e67e9c4.jpg" ) );
            data.put( "photo", photo );
            data.put( "backgroundImage", null );
            data.put(
                    "about",
                    "I am Java/Groovy developer with more than 10 years of experience in software design and development. I has been involved in several high load web projects. Also I am contributing some open source projects.\nAll free time I dedicate to my family. We love travelling in new interesting places." );
            data.put( "friendCount", 90 );
            data.put( "followerCount", 152 );
            data.put( "locked", false );
            data.put( "country", "US" );
            data.put( "location", "San Francisco, CA" );
            Map<String, Object> place1 = new LinkedHashMap<>();
            place1.put( "name", "home" );
            place1.put( "geo", "ec31e2c1acbbe1bee" );
            place1.put( "lastCheckin", new Date() );
            Map<String, Object> place2 = new LinkedHashMap<>();
            place2.put( "name", "work" );
            place2.put( "geo", "df51dec6f4ee2b2c" );
            place2.put( "lastCheckin", new Date() );
            data.put( "places", Arrays.asList( place1, place2 ) );
            data.put( "createDate", new Date() );
            data.put( "citationIndex", 2.5 );

            return data;
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    private static final List<Object> COMPLEX_DATA = generateComplexData();

    private static List<Object> generateComplexData() {
        List<Object> data = new ArrayList<>();

        Map<String, Object> message1 = new LinkedHashMap<>();
        message1.put( "id", "4f16fc97d1e2d32371003f02" );
        message1.put(
                "body",
                "COURTYARD\n\nMESQUITE\n2300 HWY 67\nMESQUITE, TX  75150\ntel: 972-681-3300\nfax: 972-681-3324\n\nHotel Information: http://courtyard.com/DALCM\n\n\nARRIVAL CONFIRMATION:\n Confirmation Number:84029698\nGuests in Room: 2\nNAME: MR ERIC  BASS \nGuest Phone: 7138530977\nNumber of Rooms:1\nArrive: Oct 6 2001\nDepart: Oct 7 2001\nRoom TypeType: ROOM - QUALITY\nGuarantee Method:\n Credit card guarantee\nCANCELLATION PERMITTED-BEFORE 1800 DAY OF ARRIVAL\n\nRATE INFORMATION:\nRate(s) Quoted inputStream: US DOLLAR\nArrival Date: Oct 6 2001\nRoom Rate: 62.10  per night. Plus tax when applicable\nRate Program: AAA AMERICAN AUTO ASSN\n\nSPECIAL REQUEST:\n NON-SMOKING ROOM, GUARANTEED\n   \n\n\nPLEASE DO NOT REPLY TO THIS EMAIL \nAny Inquiries Please call 1-800-321-2211 or your local\ninternational toll free number.\n \nConfirmation Sent: Mon Jul 30 18:19:39 2001\n\nLegal Disclaimer:\nThis confirmation notice has been transmitted to you by electronic\nmail for your convenience. Marriott's record of this confirmation\nnotice is the official record of this reservation. Subsequent\nalterations to this electronic message after its transmission\nwill be disregarded.\n\nMarriott is pleased to announce that High Speed Internet Access is\nbeing rolled out in all Marriott hotel brands around the world.\nTo learn more or to find out whether your hotel has the service\navailable, please visit Marriott.com.\n\nEarn points toward free vacations, or frequent flyer miles\nfor every stay you make!  Just provide your Marriott Rewards\nmembership number at check inputStream.  Not yet a member?  Join for free at\nhttps://member.marriottrewards.com/Enrollments/enroll.asp?source=MCRE\n\n" );
        message1.put( "filename", "2." );
        Map<String, Object> headers1 = new LinkedHashMap<>();
        headers1.put( "Content-Transfer-Encoding", "7bit" );
        headers1.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers1.put( "Date", new Date() );
        headers1.put( "From", "reservations@marriott.com" );
        headers1.put( "Message-ID", "<32788362.1075840323896.JavaMail.evans@thyme>" );
        headers1.put( "Mime-Version", "1.0" );
        headers1.put( "Subject", "84029698 Marriott  Reservation Confirmation Number" );
        headers1.put( "To", new String[]{ "ebass@enron.com" } );
        headers1.put( "X-FileName", "eric bass 6-25-02.PST" );
        headers1.put( "X-Folder", "\\ExMerge - Bass, Eric\\Personal" );
        headers1.put( "X-From", "Reservations@Marriott.com" );
        headers1.put( "X-Origin", "BASS-E" );
        headers1.put( "X-To", "EBASS@ENRON.COM" );
        headers1.put( "X-bcc", "" );
        headers1.put( "X-cc", "" );
        message1.put( "headers", headers1 );
        message1.put( "mailbox", "bass-e" );
        message1.put( "subFolder", "personal" );
        data.add( message1 );

        Map<String, Object> message2 = new LinkedHashMap<>();
        message2.put( "id", "4f16fc97d1e2d32371003f04" );
        message2.put(
                "body",
                "\nAUSTIN AT THE CAPITOL\n701 EAST 11TH STREET\nAUSTIN, TX  78701\ntel: 512-478-1111\nfax: 512-478-3700\n\nHotel Information: http://marriotthotels.com/AUSDT\n\n\nARRIVAL CONFIRMATION:\n Confirmation Number:83929275\nGuests in Room: 3\nNAME: MR ERIC  BASS \nGuest Phone: 7138530977\nNumber of Rooms:1\nArrive: Sep 7 2001\nDepart: Sep 8 2001\nRoom TypeType: ROOM - QUALITY\nGuarantee Method:\n Credit card guarantee\n\n\nRATE INFORMATION:\nRate(s) Quoted inputStream: US DOLLAR\nArrival Date: Sep 7 2001\nRoom Rate: 129.00  per night. Plus tax when applicable\nRate Program: LEISURE RATE\n\nSPECIAL REQUEST:\n NON-SMOKING ROOM, GUARANTEED\n 2 DOUBLE BEDS, GUARANTEED\n HIGH FLOOR, REQUEST NOTED\n \n\n\nPLEASE DO NOT REPLY TO THIS EMAIL \nAny Inquiries Please call 1-800-228-9290 or your local\ninternational toll free number.\n \nConfirmation Sent: Mon Jul 30 15:45:05 2001\n\nLegal Disclaimer:\nThis confirmation notice has been transmitted to you by electronic\nmail for your convenience. Marriott's record of this confirmation\nnotice is the official record of this reservation. Subsequent\nalterations to this electronic message after its transmission\nwill be disregarded.\n\nMarriott is pleased to announce that High Speed Internet Access is\nbeing rolled out in all Marriott hotel brands around the world.\nTo learn more or to find out whether your hotel has the service\navailable, please visit Marriott.com.\n\nEarn points toward free vacations, or frequent flyer miles\nfor every stay you make!  Just provide your Marriott Rewards\nmembership number at check inputStream.  Not yet a member?  Join for free at\nhttps://member.marriottrewards.com/Enrollments/enroll.asp?source=MCRE\n\n" );
        message2.put( "filename", "3." );
        Map<String, Object> headers2 = new LinkedHashMap<>();
        headers2.put( "Content-Transfer-Encoding", "7bit" );
        headers2.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers2.put( "Date", new Date() );
        headers2.put( "From", "reservations@marriott.com" );
        headers2.put( "Message-ID", "<9573108.1075840323920.JavaMail.evans@thyme>" );
        headers2.put( "Mime-Version", "1.0" );
        headers2.put( "Subject", "83929275 Marriott  Reservation Confirmation Number" );
        headers2.put( "To", new String[]{ "ebass@enron.com" } );
        headers2.put( "X-FileName", "eric bass 6-25-02.PST" );
        headers2.put( "X-Folder", "\\ExMerge - Bass, Eric\\Personal" );
        headers2.put( "X-From", "Reservations@Marriott.com" );
        headers2.put( "X-Origin", "BASS-E" );
        headers2.put( "X-To", "EBASS@ENRON.COM" );
        headers2.put( "X-bcc", "" );
        headers2.put( "X-cc", "" );
        message2.put( "headers", headers2 );
        message2.put( "mailbox", "bass-e" );
        message2.put( "subFolder", "personal" );
        data.add( message2 );

        Map<String, Object> message3 = new LinkedHashMap<>();
        message3.put( "id", "4f16fc97d1e2d32371003f05" );
        message3.put(
                "body",
                "\nAUSTIN AT THE CAPITOL\n701 EAST 11TH STREET\nAUSTIN, TX  78701\ntel: 512-478-1111\nfax: 512-478-3700\n\nHotel Information: http://marriotthotels.com/AUSDT\n\n\nARRIVAL CONFIRMATION:\n Confirmation Number:83929275\nGuests in Room: 3\nNAME: MR ERIC  BASS \nGuest Phone: 7138530977\nNumber of Rooms:1\nArrive: Sep 7 2001\nDepart: Sep 8 2001\nRoom TypeType: ROOM - QUALITY\nGuarantee Method:\n Credit card guarantee\n\n\nRATE INFORMATION:\nRate(s) Quoted inputStream: US DOLLAR\nArrival Date: Sep 7 2001\nRoom Rate: 129.00  per night. Plus tax when applicable\nRate Program: LEISURE RATE\n\nSPECIAL REQUEST:\n NON-SMOKING ROOM, GUARANTEED\n 2 DOUBLE BEDS, GUARANTEED\n HIGH FLOOR, REQUEST NOTED\n \n\n\nPLEASE DO NOT REPLY TO THIS EMAIL \nAny Inquiries Please call 1-800-228-9290 or your local\ninternational toll free number.\n \nConfirmation Sent: Mon Jul 30 15:45:05 2001\n\nLegal Disclaimer:\nThis confirmation notice has been transmitted to you by electronic\nmail for your convenience. Marriott's record of this confirmation\nnotice is the official record of this reservation. Subsequent\nalterations to this electronic message after its transmission\nwill be disregarded.\n\nMarriott is pleased to announce that High Speed Internet Access is\nbeing rolled out in all Marriott hotel brands around the world.\nTo learn more or to find out whether your hotel has the service\navailable, please visit Marriott.com.\n\nEarn points toward free vacations, or frequent flyer miles\nfor every stay you make!? Just provide your Marriott Rewards\nmembership number at check inputStream.? Not yet a member?? Join for free at\nhttps://member.marriottrewards.com/Enrollments/enroll.asp?source=MCRE\n\n" );
        message3.put( "filename", "33." );
        Map<String, Object> headers3 = new LinkedHashMap<>();
        headers3.put( "Content-Transfer-Encoding", "7bit" );
        headers3.put( "Content-Type", "text/plain; charset=ANSI_X3.4-1968" );
        headers3.put( "Date", new Date() );
        headers3.put( "From", "reservations@marriott.com" );
        headers3.put( "Message-ID", "<18215700.1075859134808.JavaMail.evans@thyme>" );
        headers3.put( "Mime-Version", "1.0" );
        headers3.put( "Subject", "83929275 Marriott  Reservation Confirmation Number" );
        headers3.put( "To", new String[]{ "ebass@enron.com" } );
        headers3.put( "X-FileName", "ebass (Non-Privileged).pst" );
        headers3.put( "X-Folder", "\\Eric_Bass_Jan2002\\Bass, Eric\\Personal" );
        headers3.put( "X-From", "Reservations@Marriott.com" );
        headers3.put( "X-Origin", "Bass-E" );
        headers3.put( "X-To", "EBASS@ENRON.COM" );
        headers3.put( "X-bcc", "" );
        headers3.put( "X-cc", "" );
        message3.put( "headers", headers3 );
        message3.put( "mailbox", "bass-e" );
        message3.put( "subFolder", "personal" );
        data.add( message3 );

        Map<String, Object> message4 = new LinkedHashMap<>();
        message4.put( "id", "4f16fc97d1e2d32371003f3e" );
        message4.put(
                "body",
                "---------------------- Forwarded by Edward D Gottlob/HOU/ECT on 04/09/2001 \n10:36 AM ---------------------------\n\n\n\"Gottlob Jr, Donald W\" <GOTTLODW@bp.com> on 04/09/2001 10:24:25 AM\nTo: \"Williamson, Alvin G\" <WILLIAA7@bp.com>, \"Befort, August III\"  \n<Beforta@bp.com>, \"'Aunt Billie'\" <tejas@txcyber.com>, \"'Bill Larson'\"  \n<BillLarson@wyoming.com>, \"'coe/jay wk'\" <stubeco@aol.com>, \"'Craig Janek'\"  \n<craigjanek@aol.com>, \"'David Smith'\" <DS1020@aol.com>, \"Richardson Jr, \nDonald B\" <RICHARDB@bp.com>, \"'Ed'\" <egottlo@ect.enron.com>, \"'Hal'\" \n<Barrow4@Prodigy.net>, \"Hooter, Howard\" <HOOTERHL@bp.com>, \"Ivy, Ross A\" \n<Ivyra@bp.com>, \"'J Carter'\" <jcarter@utmb.edu>, \"'J Snider'\"  \n<john_snider@sgs.com>, \"Guyton, Jimmie V\" <GUYTONJV@bp.com>, \"'Jan Payne'\"  \n<jpayne@hydril.com>, \"'Jeff hm'\" <bayshore2@earthlink.net>, \"'jeff work'\"  \n<GottlobJW@2mawnr.usmc.mil>, \"'Jessie'\" <nowir1x1@aol.com>, \"'John Garza'\"  \n<ajgarza@msn.com>, \"Geddes, John T\" <GEDDESJT@bp.com>, \"Clark, Ken\"  \n<CLARKRK@bp.com>, \"Payne, Kenneth M\" <PAYNEKM@bp.com>, \"'koral'\"  \n<kkypreos@yahoo.com>, \"'Kyle'\" <kyle.johnston@anico.com>, \"Frank, Larry J\"  \n<FRANKLJ@bp.com>, \"'Leigh'\" <LeighRV@aol.com>, \"'louie'\"  \n<trochesset@aol.com>, \"'lynn'\" <ljamail@galvestoncvb.com>, \"Painter, Martin \nD\" <PAINTEMD@bp.com>, \"'melinda'\"  <mlgarlan@email.utmb.edu>, \"Lozano, \nMichael A\" <LOZANOMA@bp.com>, \"'paul hm'\" <pwhitewing@aol.com>, \"'paul wk'\" \n<pwhiteman@endurosys.com>, \"Eames, Richard D\" <eamesrd@bp.com>, \"'R.Novelli'\" \n<ross@txlending.com>, \"Pinder, Charles R\" <PINDERCR@bp.com>, \"'Raul'\" \n<rrcastro@utmb.edu>, \"Moseley, Robert L\" <MOSELERL@bp.com>, \"'Ronnie'\" \n<rch965@aol.com>, \"'s garza'\" <sgarza@endurosys.com>, \"Tenhaaf, Stephen P.\" \n<tenhaasp@bp.com>, \"'stacey'\" <sgottlob@utmb.edu>, \"Douat, Stephen F\" \n<DOUATSF@bp.com>, \"'steve levy'\" <slevy@wt.net>, \"'susan garza'\" \n<segw99@aol.com>, \"'Tina (wk)'\" <Tina.walker@tdh.state.tx.us>, \"Fox, Vernon \nD\"  <FOXVD@bp.com>, \"Daley, Vicki R\" <DALEYVR@bp.com>, \"Blundell, Wayne\"  \n<BLUNDENW@bp.com>, \"Larson, William L\" <LARSONWL@bp.com>\ncc:  \nSubject: FW: Check this out.\n\n\n\n?\n-----Original Message-----\nFrom: Barnes, Mark T  \nSent: Saturday, March 17, 2001 03:29\nTo: Gottlob Jr, Donald  W; Evans, Jay\nSubject: FW: Check this out.\n\n\n\n\n?\n - HAPPY.EXE\n\n\n" );
        message4.put( "filename", "1041." );
        Map<String, Object> headers4 = new LinkedHashMap<>();
        headers4.put( "Content-Transfer-Encoding", "7bit" );
        headers4.put( "Content-Type", "text/plain; charset=ANSI_X3.4-1968" );
        headers4.put( "Date", new Date() );
        headers4.put( "From", "eric.bass@enron.com" );
        headers4.put( "Message-ID", "<1543176.1075854772155.JavaMail.evans@thyme>" );
        headers4.put( "Mime-Version", "1.0" );
        headers4.put( "Subject", "FW: Check this out." );
        headers4.put( "To", new String[]{ "shanna.husser@enron.com", "bryan.hull@enron.com", "timothy.blanchard@enron.com", "matthew.lenhart@enron.com",
                "david.baumbach@enron.com", "phillip.love@enron.com", "patrick.ryder@enron.com" } );
        headers4.put( "X-FileName", "ebass.nsf" );
        headers4.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers4.put( "X-From", "Eric Bass" );
        headers4.put( "X-Origin", "Bass-E" );
        headers4.put( "X-To", "Shanna Husser, Bryan Hull, Timothy Blanchard, Matthew Lenhart, David Baumbach, Phillip M Love, Patrick Ryder" );
        headers4.put( "X-bcc", "" );
        headers4.put( "X-cc", "" );
        message4.put( "headers", headers4 );
        message4.put( "mailbox", "bass-e" );
        message4.put( "subFolder", "sent" );
        data.add( message4 );

        Map<String, Object> message5 = new LinkedHashMap<>();
        message5.put( "id", "4f16fc97d1e2d32371003f3f" );
        message5.put(
                "body",
                "thanks\n\n\n\n\nEdward D Gottlob\n04/09/2001 10:36 AM\nTo: Eric Bass/HOU/ECT@ECT\ncc:  \nSubject: FW: Check this out.\n\n\n---------------------- Forwarded by Edward D Gottlob/HOU/ECT on 04/09/2001 \n10:36 AM ---------------------------\n\n\n\"Gottlob Jr, Donald W\" <GOTTLODW@bp.com> on 04/09/2001 10:24:25 AM\nTo: \"Williamson, Alvin G\" <WILLIAA7@bp.com>, \"Befort, August III\"  \n<Beforta@bp.com>, \"'Aunt Billie'\" <tejas@txcyber.com>, \"'Bill Larson'\"  \n<BillLarson@wyoming.com>, \"'coe/jay wk'\" <stubeco@aol.com>, \"'Craig Janek'\"  \n<craigjanek@aol.com>, \"'David Smith'\" <DS1020@aol.com>, \"Richardson Jr, \nDonald B\" <RICHARDB@bp.com>, \"'Ed'\" <egottlo@ect.enron.com>, \"'Hal'\" \n<Barrow4@Prodigy.net>, \"Hooter, Howard\" <HOOTERHL@bp.com>, \"Ivy, Ross A\" \n<Ivyra@bp.com>, \"'J Carter'\" <jcarter@utmb.edu>, \"'J Snider'\"  \n<john_snider@sgs.com>, \"Guyton, Jimmie V\" <GUYTONJV@bp.com>, \"'Jan Payne'\"  \n<jpayne@hydril.com>, \"'Jeff hm'\" <bayshore2@earthlink.net>, \"'jeff work'\"  \n<GottlobJW@2mawnr.usmc.mil>, \"'Jessie'\" <nowir1x1@aol.com>, \"'John Garza'\"  \n<ajgarza@msn.com>, \"Geddes, John T\" <GEDDESJT@bp.com>, \"Clark, Ken\"  \n<CLARKRK@bp.com>, \"Payne, Kenneth M\" <PAYNEKM@bp.com>, \"'koral'\"  \n<kkypreos@yahoo.com>, \"'Kyle'\" <kyle.johnston@anico.com>, \"Frank, Larry J\"  \n<FRANKLJ@bp.com>, \"'Leigh'\" <LeighRV@aol.com>, \"'louie'\"  \n<trochesset@aol.com>, \"'lynn'\" <ljamail@galvestoncvb.com>, \"Painter, Martin \nD\" <PAINTEMD@bp.com>, \"'melinda'\"  <mlgarlan@email.utmb.edu>, \"Lozano, \nMichael A\" <LOZANOMA@bp.com>, \"'paul hm'\" <pwhitewing@aol.com>, \"'paul wk'\" \n<pwhiteman@endurosys.com>, \"Eames, Richard D\" <eamesrd@bp.com>, \"'R.Novelli'\" \n<ross@txlending.com>, \"Pinder, Charles R\" <PINDERCR@bp.com>, \"'Raul'\" \n<rrcastro@utmb.edu>, \"Moseley, Robert L\" <MOSELERL@bp.com>, \"'Ronnie'\" \n<rch965@aol.com>, \"'s garza'\" <sgarza@endurosys.com>, \"Tenhaaf, Stephen P.\" \n<tenhaasp@bp.com>, \"'stacey'\" <sgottlob@utmb.edu>, \"Douat, Stephen F\" \n<DOUATSF@bp.com>, \"'steve levy'\" <slevy@wt.net>, \"'susan garza'\" \n<segw99@aol.com>, \"'Tina (wk)'\" <Tina.walker@tdh.state.tx.us>, \"Fox, Vernon \nD\"  <FOXVD@bp.com>, \"Daley, Vicki R\" <DALEYVR@bp.com>, \"Blundell, Wayne\"  \n<BLUNDENW@bp.com>, \"Larson, William L\" <LARSONWL@bp.com>\ncc:  \nSubject: FW: Check this out.\n\n\n\n?\n-----Original Message-----\nFrom: Barnes, Mark T  \nSent: Saturday, March 17, 2001 03:29\nTo: Gottlob Jr, Donald  W; Evans, Jay\nSubject: FW: Check this out.\n\n\n\n\n?\n - HAPPY.EXE\n\n\n\n" );
        message5.put( "filename", "1042." );
        Map<String, Object> headers5 = new LinkedHashMap<>();
        headers5.put( "Content-Transfer-Encoding", "7bit" );
        headers5.put( "Content-Type", "text/plain; charset=ANSI_X3.4-1968" );
        headers5.put( "Date", new Date() );
        headers5.put( "From", "eric.bass@enron.com" );
        headers5.put( "Message-ID", "<32137540.1075854772177.JavaMail.evans@thyme>" );
        headers5.put( "Mime-Version", "1.0" );
        headers5.put( "Subject", "Re: FW: Check this out." );
        headers5.put( "To", new String[]{ "edward.gottlob@enron.com" } );
        headers5.put( "X-FileName", "ebass.nsf" );
        headers5.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers5.put( "X-From", "Eric Bass" );
        headers5.put( "X-Origin", "Bass-E" );
        headers5.put( "X-To", "Edward D Gottlob" );
        headers5.put( "X-bcc", "" );
        headers5.put( "X-cc", "" );
        message5.put( "headers", headers5 );
        message5.put( "mailbox", "bass-e" );
        message5.put( "subFolder", "sent" );
        data.add( message5 );

        Map<String, Object> message6 = new LinkedHashMap<>();
        message6.put( "id", "4f16fc97d1e2d32371003f40" );
        message6.put(
                "body",
                "Brett,\n\nJust thought I'd drop you a line.  Cool site, I guess you have too much spare \ntime on your hands.  \nNothing too exciting is going on here in h-town - just working.  I am trading \ngas futures and other derivatives for Enron- it can get exciting at times \n(lots of $ flying trading hands), but I haven't got to blow anything up like \nyou, so I feel that my life is somewhat lacking.  \nBig news on Marilyn - I had thought that you broke up before your foray to \nFt. Sill.  Do you get to see each other much?  \n\nWhere are you currently stationed? \n\nDrop me a line next time you come to houston?\n\n-out\n\n\n\n\n\"Brett Lawler\" <brettlawler@hotmail.com> on 04/08/2001 04:13:40 PM\nTo: \ncc:  \nSubject: Got bored and...\n\n\nHey y'all,\n\nGot bored, made a homepage, lemme know what you think.\n\naddress is:  http://lawlerspage.homestead.com/\n\nI'm not sure but I think if I edit or add stuff to the page you have to\nre-enter the address if u had it bookmarked so I reckon if u bookmark it and\nit don't work it's cause I added some stuff.  Either that or it was just a\nfreak one time thing (I tried to use my bookmark today and it no workie).\n\nLawler, OUT.\n_________________________________________________________________\nGet your FREE download of MSN Explorer at http://explorer.msn.com\n\n\n" );
        message6.put( "filename", "1043." );
        Map<String, Object> headers6 = new LinkedHashMap<>();
        headers6.put( "Content-Transfer-Encoding", "7bit" );
        headers6.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers6.put( "Date", new Date() );
        headers6.put( "From", "eric.bass@enron.com" );
        headers6.put( "Message-ID", "<9944177.1075854772199.JavaMail.evans@thyme>" );
        headers6.put( "Mime-Version", "1.0" );
        headers6.put( "Subject", "Re: Got bored and..." );
        headers6.put( "To", new String[]{ "brettlawler@hotmail.com" } );
        headers6.put( "X-FileName", "ebass.nsf" );
        headers6.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers6.put( "X-From", "Eric Bass" );
        headers6.put( "X-Origin", "Bass-E" );
        headers6.put( "X-To", "\"Brett Lawler\" <brettlawler@hotmail.com> @ ENRON" );
        headers6.put( "X-bcc", "" );
        headers6.put( "X-cc", "" );
        message6.put( "headers", headers6 );
        message6.put( "mailbox", "bass-e" );
        message6.put( "subFolder", "sent" );
        data.add( message6 );

        Map<String, Object> message7 = new LinkedHashMap<>();
        message7.put( "id", "4f16fc97d1e2d32371003f41" );
        message7.put(
                "body",
                "---------------------- Forwarded by Eric Bass/HOU/ECT on 04/06/2001 03:16 PM \n---------------------------\n\n\nBryan Hull\n04/06/2001 02:21 PM\nTo: Eric Bass/HOU/ECT@ECT, Matthew Lenhart/HOU/ECT@ECT, David \nBaumbach/HOU/ECT@ECT, hullkron@bellsouth.net, Michael Walters/HOU/ECT@ECT, \nO'Neal D Winfree/HOU/ECT@ECT, Phillip M Love/HOU/ECT@ECT\ncc:  \nSubject: FW: New PG&E line Trucks\n\n\n\n>  <<2001 Line Trucks.jpg>>\n>\n> When the California energy crisis started to get press coverage back inputStream\n> November 2000, a new PG&E policy was implemented to remove all company\n> logos\n> from vehicles for personnel/property protection (no kidding), and to stop\n> using the blue paint on new vehicles as a cost reduction strategy.  How\n> dare\n> they say we're not a responsible electric provider!\n>\n> >            Due to the current budget cut backs in the company we had to\n> > drop some of the options that we normally order on the crew trucks.\n> > Attached you will find the new standard replacement vehicle, as you can\n> > see were able to keep the  PG&E corporate color and stay within budget.\n>\n\n\n\n - 2001 Line Trucks.jpg\n\n\n\n\n\n\n\n\n" );
        message7.put( "filename", "1044." );
        Map<String, Object> headers7 = new LinkedHashMap<>();
        headers7.put( "Content-Transfer-Encoding", "7bit" );
        headers7.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers7.put( "Date", new Date() );
        headers7.put( "From", "eric.bass@enron.com" );
        headers7.put( "Message-ID", "<30727575.1075854772221.JavaMail.evans@thyme>" );
        headers7.put( "Mime-Version", "1.0" );
        headers7.put( "Subject", "FW: New PG&E line Trucks" );
        headers7.put( "To", new String[]{ "jim.schwieger@enron.com", "thomas.martin@enron.com" } );
        headers7.put( "X-FileName", "ebass.nsf" );
        headers7.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers7.put( "X-From", "Eric Bass" );
        headers7.put( "X-Origin", "Bass-E" );
        headers7.put( "X-To", "Jim Schwieger, Thomas A Martin" );
        headers7.put( "X-bcc", "" );
        headers7.put( "X-cc", "" );
        message7.put( "headers", headers7 );
        message7.put( "mailbox", "bass-e" );
        message7.put( "subFolder", "sent" );
        data.add( message7 );

        Map<String, Object> message8 = new LinkedHashMap<>();
        message8.put( "id", "4f16fc97d1e2d32371003f42" );
        message8.put(
                "body",
                "Hey Gillette,\n\nThe heat rate is going to depend on the type of fuel and the construction \ndate of the unit.  Unfortunately, most of that info is proprietary.  \n\nChris Gaskill is the head of our fundamentals group and he might be able to \nsupply you with some of the guidelines.\n\n-Bass\n\n\n   \n\tEnron North America Corp.\n\t\n\tFrom:  Lisa Gillette                           04/05/2001 02:31 PM\n\t\n\nTo: Eric Bass/HOU/ECT@ECT\ncc:  \nSubject: Power Generation Question\n\nHey Bass,\n\nI have a question and I am hoping you can help me.  I am wanting to compile a \nlist of all the different types of power plants and their respective heat \nrates to determine some sort of generation ratio.\n\ni.e. Coal  4 mmbtu = 1 MW\n Simple Cycle 11 mmbtu = 1 MW\n\nPlease let me know if you can help me or point me to someone who can.  Just \nFYI...Bryan suggested that I call you so blame him as you curse me under your \nbreath right now.\n\nThanks,\nLisa\n\n" );
        message8.put( "filename", "1045." );
        Map<String, Object> headers8 = new LinkedHashMap<>();
        headers8.put( "Content-Transfer-Encoding", "7bit" );
        headers8.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers8.put( "Date", new Date() );
        headers8.put( "From", "eric.bass@enron.com" );
        headers8.put( "Message-ID", "<2106897.1075854772243.JavaMail.evans@thyme>" );
        headers8.put( "Mime-Version", "1.0" );
        headers8.put( "Subject", "Re: Power Generation Question" );
        headers8.put( "To", new String[]{ "lisa.gillette@enron.com" } );
        headers8.put( "X-FileName", "ebass.nsf" );
        headers8.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers8.put( "X-From", "Eric Bass" );
        headers8.put( "X-Origin", "Bass-E" );
        headers8.put( "X-To", "Lisa Gillette" );
        headers8.put( "X-bcc", "" );
        headers8.put( "X-cc", "" );
        message8.put( "headers", headers8 );
        message8.put( "mailbox", "bass-e" );
        message8.put( "subFolder", "sent" );
        data.add( message8 );

        Map<String, Object> message9 = new LinkedHashMap<>();
        message9.put( "id", "4f16fc97d1e2d32371003f46" );
        message9.put(
                "body",
                "my address is :\n2302 Travis St. #8225\n77006\n\n\n\nTo: Fiji, Eric Bass/HOU/ECT@ECT, Micah Hatten/HOU/EES@EES\ncc:  \nSubject: Addresses\n\nAll:\n\nLouise will kick my ass if I don't have your addresses by the end of the \nweek.  Please send them ASAP.  Reminder:  The wedding is Saturday October 20, \n2001 in New Orleans.  Please forward to Shawn, Dietz, Jacques, and Maziarz - \nI don't have their e-mail.  I'll see most of you this weekend.\n\nTim\n\n" );
        message9.put( "filename", "1049." );
        Map<String, Object> headers9 = new LinkedHashMap<>();
        headers9.put( "Content-Transfer-Encoding", "7bit" );
        headers9.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers9.put( "Date", new Date() );
        headers9.put( "From", "eric.bass@enron.com" );
        headers9.put( "Message-ID", "<8467495.1075854772328.JavaMail.evans@thyme>" );
        headers9.put( "Mime-Version", "1.0" );
        headers9.put( "Subject", "Re: Addresses" );
        headers9.put( "To", new String[]{ "timothy.blanchard@enron.com" } );
        headers9.put( "X-FileName", "ebass.nsf" );
        headers9.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers9.put( "X-From", "Eric Bass" );
        headers9.put( "X-Origin", "Bass-E" );
        headers9.put( "X-To", "Timothy Blanchard" );
        headers9.put( "X-bcc", "" );
        headers9.put( "X-cc", "" );
        message9.put( "headers", headers9 );
        message9.put( "mailbox", "bass-e" );
        message9.put( "subFolder", "sent" );
        data.add( message9 );

        Map<String, Object> message10 = new LinkedHashMap<>();
        message10.put( "id", "4f16fc97d1e2d32371003f50" );
        message10
                .put( "body",
                        "gosset is going to fire me?\n\n\n\n\nPatrick Ryder@ENRON\n03/28/2001 01:49 PM\nTo: David Baumbach/HOU/ECT@ECT, O'Neal D Winfree/HOU/ECT@ECT, Patrick \nRyder/NA/Enron@Enron, Ryan O'Rourke/ENRON@enronXgate, Darron C \nGiron/HOU/ECT@ECT, Phillip M Love/HOU/ECT@ECT, Jeffrey C Gossett/HOU/ECT@ECT, \nBryan Hull/HOU/ECT@ECT, Kevin Bosse/HOU/ECT@ECT, B Scott Palmer/HOU/ECT@ECT, \nEric Bass/HOU/ECT@ECT\ncc:  \nSubject: Softball\n\n\nGentlemen,\n\nThe following are links that have a map and a bracket for the tournament. \nOur first game is at 8:30 AM on Sunday. We have already lost Darren for the \nwhole tournament and Gosset for at least the first game. \nEveryone must be there on TIME. That means that you must be at the fields no \nlater than 8 AM! We now only have 10 guys for the first game!!!!!!!\n\nIf you can't make it at 8 AM, tell me know so we can replace you!!!!! \nEVERYONE e-mail me and let me know that you will be there by 8 AM! Gosset \nwill fire you if you screw the team!!!!  \n\n\nhttp://home.enron.com:84/esa/Men.htm\n\n\n\nhttp://home.enron.com:84/esa/page7.html\n\n\n" );
        message10.put( "filename", "1058." );
        Map<String, Object> headers10 = new LinkedHashMap<>();
        headers10.put( "Content-Transfer-Encoding", "7bit" );
        headers10.put( "Content-Type", "text/plain; charset=us-ascii" );
        headers10.put( "Date", new Date() );
        headers10.put( "From", "eric.bass@enron.com" );
        headers10.put( "Message-ID", "<26946157.1075854772551.JavaMail.evans@thyme>" );
        headers10.put( "Mime-Version", "1.0" );
        headers10.put( "Subject", "Re: Softball" );
        headers10.put( "To", new String[]{ "patrick.ryder@enron.com" } );
        headers10.put( "X-FileName", "ebass.nsf" );
        headers10.put( "X-Folder", "\\Eric_Bass_Jun2001\\Notes Folders\\Sent" );
        headers10.put( "X-From", "Eric Bass" );
        headers10.put( "X-Origin", "Bass-E" );
        headers10.put( "X-To", "Patrick Ryder" );
        headers10.put( "X-bcc", "" );
        headers10.put( "X-cc", "" );
        message10.put( "headers", headers10 );
        message10.put( "mailbox", "bass-e" );
        message10.put( "subFolder", "sent" );
        data.add( message10 );

        return data;
    }


    @Test
    public void serializeIntArray() {
        JsonSerializer serializer = new JsonSerializerFactory().create();
        serializer.serialize( new int[]{ 0, 1, 2, 3, 4, 5 } );
    }


    @Test
    public void testWithStringArray(){
        String[] cats = new String[10];
        cats[0] = "Felix";
        cats[5] = "Tom";
        String sRick = new JsonSimpleSerializerImpl().serialize(cats).toString();
        boolean ok = sRick.equals( "[\"Felix\",null,null,null,null,\"Tom\",null,null,null,null]" ) || die( sRick );
    }



    @Test
    public void testWithIntArray(){
        int[] numbers = new int[10];
        numbers[0] = 5;
        numbers[3] = 10;

        String sRick = new JsonSimpleSerializerImpl().serialize(numbers).toString();
        int[] numeros = new JsonParserFactory().create().parseIntArray(sRick);

        Assert.assertArrayEquals(numbers, numeros);
    }



}
