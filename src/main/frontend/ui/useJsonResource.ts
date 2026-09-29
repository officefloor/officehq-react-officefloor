import { useCallback, useEffect, useState } from 'react';

// Load a JSON resource from `url` into state, refetching whenever the url changes, and hand back a
// `reload` for callers that mutate the resource and need to pull the fresh version. Feature panels
// read one url this way, so this shared primitive is where that fetch-into-state pattern lives once.
export function useJsonResource<T>(url: string, initial: T): [T, () => Promise<void>] {
  const [data, setData] = useState<T>(initial);
  const reload = useCallback(async () => {
    const res = await fetch(url);
    setData(await res.json());
  }, [url]);
  useEffect(() => {
    void reload();
  }, [reload]);
  return [data, reload];
}
