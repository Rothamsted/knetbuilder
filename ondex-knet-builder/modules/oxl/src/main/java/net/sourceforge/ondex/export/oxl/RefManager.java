package net.sourceforge.ondex.export.oxl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 06-Mar-2010
 * Time: 20:39:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class RefManager<ID, T>
{
    private final Map<ID, T> idValues = new HashMap<ID,T>();
    private final Map<ID, Set<OnCompletion<T>>> completionActions = new HashMap<ID, Set<OnCompletion<T>>>();
    private final Map<ID, Callable<T>> resolvers = new HashMap<ID, Callable<T>>();

    public interface Ref<T> {
        public void setCompletionAction(OnCompletion<T> action);
        public T get();
    }

    public interface OnCompletion<T> {
        public void onCompletion(T t);
    }

    protected abstract T resolveExternally(ID id);

    public boolean anyRefsPending()
    {
        return !completionActions.isEmpty();
    }

    public Ref<T> makeRef(final ID id) {
        return new Ref<T>()
        {
            @Override
            public void setCompletionAction(OnCompletion<T> action)
            {
                attemptResolution(id);
                if(idValues.containsKey(id)) {
                    action.onCompletion(idValues.get(id));
                }
                else
                {
                    Set<OnCompletion<T>> acts = completionActions.get(id);
                    if(acts == null) {
                        acts = new HashSet<OnCompletion<T>>();
                        completionActions.put(id, acts);
                    }
                    acts.add(action);
                }
            }

            @Override
            public T get()
            {
                attemptResolution(id);
                T t = idValues.get(id);
                if(t == null) throw new IllegalStateException("Unable to resolve id: " + id);
                return t;
            }
        };
    }

    public void resolve(ID id, T t) {
        idValues.put(id, t);
        attemptResolution(id);
    }

    private boolean attemptResolution(ID id)
    {
        T t = idValues.get(id);
        if(t == null) {
            try {
                Callable<T> resolver = resolvers.get(id);
                if(resolver != null) t = resolver.call();
            }
            catch (Exception e) {
                throw new Error(e);
            }
        }
        if(t == null)
        {
            t = resolveExternally(id);
        }
        if(t != null) {
            idValues.put(id, t);
            resolvers.remove(id);
            if(completionActions.containsKey(id))
            {
                for(OnCompletion<T> oc : completionActions.get(id)) {
                    oc.onCompletion(t);
                }
                completionActions.get(id).clear();
            }
            return true;
        } else {
            return false;
        }
    }
}
