package io.advantageous.qbit.guice;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;

/**
 * Created by marat on 12/29/15
 */
@RequestMapping("/user")
public class UserEndpoint {
    @RequestMapping("/register")
    @Auth
    public SessionModel register(
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) {
        return new SessionModel("SESSION_ID");
    }

    public static class SessionModel {
        private String session;

        public SessionModel(String session) {
            this.session = session;
        }

        public String getSession() {
            return session;
        }

        public SessionModel setSession(String session) {
            this.session = session;
            return this;
        }
    }
}
