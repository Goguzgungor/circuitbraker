
# Circuit Breaker Pattern ve Uygulama Rehberi

Chris Richardson'ın "Microservices Patterns" kitabını okurken bu desenle tanıştım ve gerçekten de ilginç buldum. Hemen sizinle paylaşmak istedim çünkü biliyorum ki, bazen sadece küçük bir desen, büyük bir etki yapabilir. Şimdi ne olduğunu tanıyıp sonra uygulanışına geçelim.

## Nedir Bu Circuit Breaker ?

Bu pattern'in 3 durumu vardır:

- **Kapalı (Closed):** "Kapalı" durumunda her şey düzgün çalışıyor. Uzak servis beklenen şekilde yanıt veriyor ve herhangi bir müdahaleye gerek yok. Bu, isteklerin kesintisiz bir şekilde akmasını ve sistem normal koşullarda çalışmasını ifade eder.

- **Açık (Open):** Devre kesici "Açık" durumuna geçtiğinde, uzak servisle bir şeylerin ters gittiğini belirtir. Servis belirli bir sayıda ardışık başarısızlık sonrasında devreye girer ve sorunlu servisin daha fazla isteği engellemesi için bir güvenlik mekanizması görevi görür. Bu aşamada gelen tüm istekler kısa devre yapılır ve servisin toparlanmasına bir süre izin verilir.

- **Yarı Açık (Half-Open):** "Yarı Açık" durumu, uzak çağrıları başlatmak için yapılan başarılı denemelerin sayısını kaydeder. Bu, uzak sunucunun çevrimiçi ve beklenen şekilde çalışıp çalışmadığını kontrol etmede yardımcı olur.

## Peki biz ne yapacağız?

Bu günkü case'imizde belli bir zaman limitinde belli bir sayıda gelen hatalı isteklerin sınırı aşılırsa o servisin servis dışı olarak gözükmesini isteyeceğiz. O yüzden şu şekilde diyebiliriz.

**Amaç :** 10 saniye içinde 5'ten fazla hatalı istek gelirse, 10 saniye boyunca servisi beklemeye al.

## Peki bunu nasıl yapacağız?

Bu kodu yazmayı düşünürken aklımda 2 seçenek vardı. Ya error handling mekanizmalarını kullanarak controller hatalarını sayıp ona göre devam edecektim ya da aspect-oriented programming ve anotasyonlar kullanarak belli end pointlerde bu işlemi gerçekleştirecektim. AOP'nin hataları yakalama konusunda isteklerimi karşılamadığını görünce bundan vazgeçtim.

## Hadi Yazalım

Öncelikle çok çok basit ama hata dönen bir end point yazıyorum. Çünkü test etmek için buna ihtiyacımız var.

```java
@PostMapping("/invokeMyMethod")
public ResponseEntity<String> MyMethod(@Valid @RequestBody(required = true) CarModel payload) {
    throw new RuntimeException("This is a test exception");
}
```
## Modeller
Controller'a yollamak için bir model yazıyorum. Siz isterseniz end pointi GET methoduna alıp o şekilde de kullanabilirsiniz. Bir sonraki yazıda validasyon hatalarını yönetmeye değineceğim, şimdiden ikisini de uygun yazmak istedim.

```java
public class CarModel {
        @NotNull
        private String name;
        @NotNull
        private String model;
        @NotNull
        private String color;
   }
```
Circuit Breaker pattern için bir model de oluşturuyorum ki ilerde hataları loglamam, kaydetmem ya da sebeplerini görmek istersem bunu kullanabilirim.

``` java
public class CircuitBreakerModel {
    private String methodName;
    private String errorMessage;
    private int count;
    private Date lastInvoked;
    private int httpStatus;
    private String payload;
}
```


## Circuit Breaker Service
Circuit Breaker için bir service oluşturup bir map ekliyorum. Uygulamanın end pointinde çıkan hataları bir mapte saklayacağım. Best practice için bu yöntemi uygulayacaksanız, siz Redis ya da Mongo gibi NoSQL veritabanları kullanabilirsiniz.

```java
public class CircuitBreakerService {
    private Map<String, List<CircuitBreakerModel>> map;

    public CircuitBreakerService() {
        this.map = new HashMap<>();
    }
```
Gelen hataları kaydetmek için bir fonksiyon yazalım. Bu method sadece son 5 hatayı tutsun.

```java
public void addMethodCount(Object payload, String methodName, int httpStatus, String errorMessage) {
        if (map.containsKey(methodName)) {
        Date currentDate = new Date();
        List<CircuitBreakerModel> recentCallsList = map.get(methodName);
        CircuitBreakerModel lastInvoked = recentCallsList.get(recentCallsList.size() - 1);

        CircuitBreakerModel circuitBreaker =  new CircuitBreakerModel(methodName, lastInvoked.getCount()+1,currentDate, httpStatus, payload.toString(),errorMessage);
        int callLimit = 5;
        long timeDifference = (currentDate.getTime() - lastInvoked.getLastInvoked().getTime());
        long difference_In_Seconds
        = (timeDifference
        / 1000)
        % 60;
        log.info("Time Difference: "+difference_In_Seconds);
        lastInvoked.setLastInvoked(currentDate);
        recentCallsList.set(recentCallsList.size() - 1, lastInvoked);
        map.put(methodName, recentCallsList);
        if(difference_In_Seconds<10){
        recentCallsList.add(circuitBreaker);
        map.put(methodName, recentCallsList);
        }

        } else {
        List<CircuitBreakerModel> recentCallsList = new ArrayList<>();
        CircuitBreakerModel circuitBreaker =  new CircuitBreakerModel(methodName,1,new Date(), httpStatus, payload.toString(),errorMessage);
        recentCallsList.add(circuitBreaker);
        map.put(methodName, recentCallsList);
        }
}
```
End pointe şu ana kadar kaç hata geldiğini gösteren bir method yazalım.

```java
public int getMethodCount(String methodName) {
        if (map.containsKey(methodName)) {
        return map.get(methodName).size();
        } else {
        return 0;
        }
}
```
Son hatanın tarihini gösteren bir method yazalım.


```java
public long LastErrorsTimeAfter5th(String methodName) {
        if (map.containsKey(methodName)) {
        if (map.get(methodName).size() >= 5) {
        List<CircuitBreakerModel> recentCallsList = map.get(methodName);
        CircuitBreakerModel CircuitBreakerModel = recentCallsList.get(4);
        return CircuitBreakerModel.getLastInvoked().getTime();
        } else {
        return 0;
        }
        } else {
        return 0;
        }
    }
```
End pointin açık olup olmadığını belirleyecek methodu yazalım.

```java 
public boolean isCircuitOpen (String methodName){
    Date currentTime = new Date();
    long timeAfter5thError = LastErrorsTimeAfter5th(methodName);
    long timeDifference = Math.abs(currentTime.getTime() - timeAfter5thError);
    long secondsDifference = timeDifference / 1000;
    int timeLimit = 10;
    if( secondsDifference<= timeLimit){
        return true;
    }else{
        this.refreshList(methodName);
        return false;
    }
}
```
Servisi tekrar açık hale getirdiğimizde arkadaki hataları da temizlememiz gerekeceği için listeyi temizleyen bir method yazalım.

```java
private void refreshList(String methodName){
if (map.containsKey(methodName)) {
List<CircuitBreakerModel> recentCallsList = map.get(methodName);
map.remove(methodName);
}
}
```
## Error Counting
Error counting için @ControllerAdvice kullanacağım. Ayrıca şu an sadece runtime ile ilgilendiğim için o hatayı ekliyorum. Siz kendi tarafınızda bunu büyük ihtimalle kendi ihtiyaçlarınıza göre değiştireceksiniz.

```java
@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    public GlobalControllerExceptionHandler(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    private final CircuitBreakerService circuitBreakerService;

    @Nullable
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        String urlpath = request.getDescription(false).replaceAll("uri=", "");
        circuitBreakerService.addMethodCount(request, urlpath, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return ResponseEntity.ok(ex.getMessage());
    }
}
```
İçine CircuitBreakerService'i bağlayıp bunu kullanmasını istedim. Ayrıca uri'yi method name olarak kullacağım, bu şekilde end pointler ayrışmış olacak.

## Filter
Hataları belli bir sayıya ulaşmış end pointleri askıya alması için de bir filter ekliyorum.

```java
@Component
public class CirciutBreakerFilter extends OncePerRequestFilter {
    int maxErrorCount = 5;
    @Autowired
    public CirciutBreakerFilter(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }
    private final CircuitBreakerService circuitBreakerService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        int count = circuitBreakerService.getMethodCount(request.getServletPath());
        if (count == maxErrorCount) {
            if (circuitBreakerService.isCircuitOpen(request.getServletPath())) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write("Service is unavailable");
                return;
            }
        }
        doFilter(request, response, filterChain);
    }
}
```
## Hadi Deneyelim
10 saniye içerisinde gelmiş 5 hataya kadar end point normal şekilde çalışmaya devam ediyor. Siz algoritmayı değiştirerek bu sürenin ve hata miktarının değişmesini sağlayabilirsiniz. Mesela her gelen başarılı istekte Circuit Breaker Pattern şartlarının sıfırlanmasını ya da 100 hataya kadar tolere etmesini isteyebilirsiniz, ben şimdilik bunu tercih edeceğim.

Şartlar sağlandıktan sonra ise 10 saniyeliğine servisimi askıya alıyoruz.

10 saniye sonra sistem yenilenmiş olarak servisimiz açılıyor.

## Sonuç
Circuit Breaker Pattern kullanıcının ve uygulamanın işini kolaylaştırmak için istediğiniz zaman kullanabileceğiniz bir pattern, umarım hoşunuza gitmiştir. İyi kodlamalar!

# Bana ulaşmak için
[Linkedin](www.linkedin.com/in/goktugoguzgungor/)
