    ____            __
   / __ \  ____    / /_   ____   ____ _
  / /_/ / / __ \  / __ \ / __ \ / __ `/
 / _, _/ / /_/ / / /_/ // /_/ // /_/ /
/_/ |_|  \____/ /_.___/ \____/ \__, /
                                 /_/

-----

> 简单易用的Android HTTP Client

-----

``` java

    new Request(GET, "http://www.google.com")
        .execute(SimpleExecutor.instance, new Request.Callback() {
            @Override
            public void onResponse(Request req, Response resp, Exception error) {
                if (error == null)
                    Log.d("tag", resp.asText());
            }
        });

```

