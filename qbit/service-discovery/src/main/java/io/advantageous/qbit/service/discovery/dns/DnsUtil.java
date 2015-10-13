package io.advantageous.qbit.service.discovery.dns;

import io.advantageous.boon.core.IO;
import io.advantageous.boon.core.Str;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a utility class that reads DNS hosts from /etc/resolv.conf.
 */
public class DnsUtil {

    public static List<URI> readDnsConf() {
        final File file = new File("/etc/resolv.conf");


        if (file.exists()) {
            final List<String> lines = IO.readLines(file);

            return lines.stream().filter(line -> line.startsWith("nameserver"))
                    .map(line ->
                    {
                        String uriToParse = line.replace("nameserver ", "").trim();
                        final String[] split = Str.split(uriToParse, ':');
                        try {

                            if (split.length==1) {
                                return new URI("dns", "", split[0], 53, "", "", "");
                            } else if (split.length >= 2){
                                return new URI("dns", "", split[0], Integer.parseInt(split[1]), "", "", "");
                            } else {
                               throw new IllegalStateException("Unable to parse URI from /etc/resolv.conf") ;
                            }
                        } catch (URISyntaxException e) {
                            throw new IllegalStateException("failed to convert to URI");
                        }

                    })
                    .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("/etc/resolv.conf not found");
        }

    }

}
