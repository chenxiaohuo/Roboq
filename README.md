#Roboq
简单易用的Android HTTP Client
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
