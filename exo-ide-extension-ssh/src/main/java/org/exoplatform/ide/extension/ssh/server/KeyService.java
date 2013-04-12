/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ide.extension.ssh.server;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.ide.extension.ssh.shared.GenKeyRequest;
import org.exoplatform.ide.extension.ssh.shared.KeyItem;
import org.exoplatform.ide.extension.ssh.shared.PublicKey;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * REST interface to SshKeyProvider.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("ide/ssh-keys")
public class KeyService {
    private final SshKeyStore keyStore;

    public KeyService(SshKeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /** Generate SSH key pair. */
    @POST
    @Path("gen")
    @RolesAllowed({"developer"})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response genKeyPair(GenKeyRequest request) {
        try {
            keyStore.genKeyPair(request.getHost(), request.getComment(), request.getPassphrase());
        } catch (SshKeyStoreException e) {
            throw new WebApplicationException(Response.serverError() //
                                                      .entity(e.getMessage()) //
                                                      .type(MediaType.TEXT_PLAIN) //
                                                      .build());
        }
        return Response.ok().build();
    }

    /** Add prepared private key. */
    @POST
    @Path("add")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @RolesAllowed({"developer"})
    public Response addPrivateKey(@Context SecurityContext security, @QueryParam("host") String host,
                                  Iterator<FileItem> iterator) {
      /*      XXX : Temporary turn-off don't work on demo site      
            if (!security.isSecure())
            {
               throw new WebApplicationException(Response.status(400)
                  .entity("Secure connection required to be able generate key. ").type(MediaType.TEXT_PLAIN).build());
            }
      */
        byte[] key = null;
        while (iterator.hasNext() && key == null) {
            FileItem fileItem = iterator.next();
            if (!fileItem.isFormField()) {
                key = fileItem.get();
            }
        }
        // Return error response in <pre> HTML tag.
        if (key == null) {
            throw new WebApplicationException(Response.ok("<pre>Can't find input file.</pre>", MediaType.TEXT_HTML)
                                                      .build());
        }

        try {
            keyStore.addPrivateKey(host, key);
        } catch (SshKeyStoreException e) {
            throw new WebApplicationException(Response.ok("<pre>" + e.getMessage() + "</pre>", MediaType.TEXT_HTML)
                                                      .build());
        }
        return Response.ok("", MediaType.TEXT_HTML).build();
    }

    /**
     * Get public key.
     *
     * @see {@link SshKeyStore#genKeyPair(String, String, String)}
     * @see {@link SshKeyStore#getPublicKey(String)}
     */
    @GET
    @RolesAllowed({"developer"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPublicKey(@Context SecurityContext security, @QueryParam("host") String host) {

      /*      XXX : Temporary turn-off don't work on demo site      
            if (!security.isSecure())
            {
               throw new WebApplicationException(Response.status(400)
                  .entity("Secure connection required to be able generate key. ").type(MediaType.TEXT_PLAIN).build());
            }
      */
        SshKey publicKey;
        try {
            publicKey = keyStore.getPublicKey(host);
        } catch (SshKeyStoreException e) {
            throw new WebApplicationException(Response.serverError() //
                                                      .entity(e.getMessage()) //
                                                      .type(MediaType.TEXT_PLAIN) //
                                                      .build());
        }
        if (publicKey == null) {
            throw new WebApplicationException(Response.status(404) //
                                                      .entity("Public key for host " + host + " not found. ") //
                                                      .type(MediaType.TEXT_PLAIN) //
                                                      .build());
        }
        return Response.ok(new PublicKey(host, new String(publicKey.getBytes())), MediaType.APPLICATION_JSON).build();
    }

    /** Remove SSH keys. */
    @GET
    @Path("remove")
    @RolesAllowed({"developer"})
    public String removeKeys(@QueryParam("host") String host, @QueryParam("callback") String calback) {
        try {
            keyStore.removeKeys(host);
        } catch (SshKeyStoreException e) {
            throw new WebApplicationException(Response.serverError() //
                                                      .entity(e.getMessage()) //
                                                      .type(MediaType.TEXT_PLAIN) //
                                                      .build());
        }
        return calback + "();";
    }

    @GET
    @Path("all")
    @RolesAllowed({"developer"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response getKeys(@Context UriInfo uriInfo) {
        try {
            Set<String> all = keyStore.getAll();
            if (all.size() > 0) {
                List<KeyItem> result = new ArrayList<KeyItem>(all.size());
                for (String host : all) {
                    boolean publicKeyExists = keyStore.getPublicKey(host) != null;
                    String getPublicKeyUrl = null;
                    if (publicKeyExists) {
                        getPublicKeyUrl =
                                uriInfo.getBaseUriBuilder().path(getClass()).queryParam("host", host).build().toString();
                    }
                    String removeKeysUrl =
                            uriInfo.getBaseUriBuilder().path(getClass(), "removeKeys").queryParam("host", host).build()
                                   .toString();

                    result.add(new KeyItem(host, getPublicKeyUrl, removeKeysUrl));
                }
                return Response.ok().entity(result).type(MediaType.APPLICATION_JSON).build();
            }
            return Response.ok(Collections.emptyList(), MediaType.APPLICATION_JSON).build();
        } catch (SshKeyStoreException e) {
            throw new WebApplicationException(Response.serverError() //
                                                      .entity(e.getMessage()) //
                                                      .type(MediaType.TEXT_PLAIN) //
                                                      .build());
        }
    }
}
