import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, tap } from 'rxjs';

/**
 * In-memory cache for HTTP GET responses.
 * Caches painting and category endpoints to reduce backend load.
 */
interface CacheEntry {
    response: HttpResponse<unknown>;
    timestamp: number;
}

const cache = new Map<string, CacheEntry>();

// Cache TTL in milliseconds
const CACHE_TTL: Record<string, number> = {
    '/api/paintings': 5 * 60 * 1000,      // 5 minutes
    '/api/categories': 30 * 60 * 1000,    // 30 minutes
};

/**
 * URLs that should be cached (prefixes)
 */
const CACHEABLE_URLS = ['/api/paintings', '/api/categories'];

/**
 * Check if a URL should be cached
 */
function isCacheable(url: string): boolean {
    return CACHEABLE_URLS.some(prefix => url.includes(prefix));
}

/**
 * Get TTL for a URL
 */
function getTTL(url: string): number {
    for (const [prefix, ttl] of Object.entries(CACHE_TTL)) {
        if (url.includes(prefix)) {
            return ttl;
        }
    }
    return 5 * 60 * 1000; // Default 5 minutes
}

/**
 * Check if a cache entry is still valid
 */
function isValid(entry: CacheEntry, ttl: number): boolean {
    return Date.now() - entry.timestamp < ttl;
}

/**
 * Clear all cached entries - call after mutations (create, update, delete)
 */
export function clearHttpCache(): void {
    cache.clear();
    console.log('[Cache] HTTP cache cleared');
}

/**
 * Clear cache entries matching a specific pattern
 */
export function clearHttpCacheByPattern(pattern: string): void {
    for (const key of cache.keys()) {
        if (key.includes(pattern)) {
            cache.delete(key);
        }
    }
    console.log(`[Cache] Cleared entries matching: ${pattern}`);
}

/**
 * HTTP Cache Interceptor
 * Caches GET requests to painting and category endpoints.
 */
export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
    // Only cache GET requests
    if (req.method !== 'GET') {
        return next(req);
    }

    // Check if this URL should be cached
    if (!isCacheable(req.urlWithParams)) {
        return next(req);
    }

    const cacheKey = req.urlWithParams;
    const ttl = getTTL(req.urlWithParams);
    const cached = cache.get(cacheKey);

    // Return cached response if valid
    if (cached && isValid(cached, ttl)) {
        console.log(`[Cache] HIT: ${cacheKey}`);
        return of(cached.response.clone());
    }

    // Make request and cache response
    return next(req).pipe(
        tap(event => {
            if (event instanceof HttpResponse && event.status === 200) {
                // Only cache JSON responses - prevent caching HTML error pages
                const contentType = event.headers.get('content-type') || '';
                if (contentType.includes('application/json')) {
                    cache.set(cacheKey, {
                        response: event.clone(),
                        timestamp: Date.now()
                    });
                    console.log(`[Cache] STORED: ${cacheKey}`);
                } else {
                    console.warn(`[Cache] SKIPPED (non-JSON): ${cacheKey}, content-type: ${contentType}`);
                }
            }
        })
    );
};
