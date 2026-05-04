// ================================================================
// COMPONENTE REACT — Recensioni Venditore
// Carica le recensioni dal backend via API REST e permette
// di aggiungere ed eliminare recensioni senza ricaricare la pagina.
// ================================================================

const { useState, useEffect } = React;

// Componente per le stelle di valutazione
function Stelle({ valore }) {
    return (
        <span style={{ color: '#ffd700', fontSize: '1.1rem' }}>
            {'★'.repeat(valore)}{'☆'.repeat(5 - valore)}
        </span>
    );
}

// Singola card di recensione
function RecensioneCard({ rec, emailUtente, ruoloUtente, onElimina }) {
    const isAutore  = emailUtente && rec.autoreEmail === emailUtente;
    const isAdmin   = ruoloUtente === 'ROLE_ADMIN';
    const puoElimina = isAutore || isAdmin;

    return (
        <div className="review-card" style={{ marginBottom: '1.2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                    <Stelle valore={rec.valutazione} />
                    <strong style={{ color: '#ccc', fontSize: '0.9rem', marginLeft: '0.5rem' }}>
                        da <span style={{ color: '#00e676' }}>{rec.autore}</span>
                    </strong>
                </div>
                {puoElimina && (
                    <button
                        onClick={() => {
                            if (window.confirm('Eliminare questo feedback?')) onElimina(rec.id);
                        }}
                        style={{ background:'none', border:'none', color:'#ff5050', cursor:'pointer', fontSize:'0.85rem', textDecoration:'underline' }}>
                        Elimina
                    </button>
                )}
            </div>
            <p style={{ color: '#ddd', marginTop: '0.6rem', lineHeight: 1.6 }}>{rec.testo}</p>
        </div>
    );
}

// Form per aggiungere una nuova recensione
function FormRecensione({ venditoreId, onAggiunta }) {
    const [valutazione, setValutazione] = useState(5);
    const [testo, setTesto] = useState('');
    const [errore, setErrore] = useState(null);
    const [invio, setInvio] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setInvio(true);
        setErrore(null);
        try {
            const resp = await fetch(`/api/recensioni/${venditoreId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ valutazione: parseInt(valutazione), testo })
            });
            const data = await resp.json();
            if (!resp.ok) {
                setErrore(data.errore || 'Errore durante il salvataggio.');
            } else {
                setTesto('');
                setValutazione(5);
                onAggiunta(data);
            }
        } catch (err) {
            setErrore('Errore di rete.');
        } finally {
            setInvio(false);
        }
    };

    return (
        <div className="form-box" style={{ marginTop: '2rem' }}>
            <h3 style={{ color: '#00e676', marginBottom: '1.2rem' }}>Lascia un feedback</h3>
            {errore && <p style={{ color: '#ff5050', marginBottom: '0.8rem' }}>⚠️ {errore}</p>}
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label className="form-label">Valutazione</label>
                    <select value={valutazione} onChange={e => setValutazione(e.target.value)}
                            className="form-input" style={{ background: 'rgba(0,0,0,0.5)' }} required>
                        <option value="5">⭐⭐⭐⭐⭐ Eccellente</option>
                        <option value="4">⭐⭐⭐⭐ Buono</option>
                        <option value="3">⭐⭐⭐ Normale</option>
                        <option value="2">⭐⭐ Scarso</option>
                        <option value="1">⭐ Pessimo</option>
                    </select>
                </div>
                <div className="form-group">
                    <label className="form-label">Commento</label>
                    <textarea value={testo} onChange={e => setTesto(e.target.value)}
                              className="form-input" rows="3"
                              placeholder="Com'è andata la trattativa?" required />
                </div>
                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={invio}>
                    {invio ? 'Pubblicazione...' : 'Pubblica Feedback'}
                </button>
            </form>
        </div>
    );
}

// Componente principale
function RecensioniApp() {
    const root = document.getElementById('recensioni-root');
    const venditoreId  = root.dataset.venditoreId;
    const emailUtente  = root.dataset.utenteEmail;
    const ruoloUtente  = root.dataset.utenteRuolo;
    const venditoreNome = root.dataset.venditoreNome;
    const canReview    = root.dataset.canReview === 'true';

    const [recensioni, setRecensioni] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`/api/recensioni/${venditoreId}`)
            .then(r => r.json())
            .then(data => { setRecensioni(data); setLoading(false); })
            .catch(() => setLoading(false));
    }, []);

    const handleElimina = async (id) => {
        const resp = await fetch(`/api/recensioni/${id}`, { method: 'DELETE' });
        if (resp.ok) setRecensioni(prev => prev.filter(r => r.id !== id));
    };

    const handleAggiunta = (nuova) => {
        setRecensioni(prev => [nuova, ...prev]);
    };

    return (
        <div>
            <h2 style={{ color: '#00e676', marginBottom: '1.5rem', textAlign: 'center' }}>
                Cosa dicono di {venditoreNome}
            </h2>

            {loading ? (
                <p style={{ color: '#666', textAlign: 'center' }}>Caricamento...</p>
            ) : recensioni.length === 0 ? (
                <div style={{ textAlign:'center', color:'#555', padding:'2rem',
                              border:'1px dashed rgba(255,255,255,0.1)', borderRadius:'12px' }}>
                    Nessun feedback ricevuto. Sii il primo a recensire!
                </div>
            ) : (
                recensioni.map(rec => (
                    <RecensioneCard key={rec.id} rec={rec}
                        emailUtente={emailUtente} ruoloUtente={ruoloUtente}
                        onElimina={handleElimina} />
                ))
            )}

            {canReview && (
                <FormRecensione venditoreId={venditoreId} onAggiunta={handleAggiunta} />
            )}

            {!canReview && !emailUtente && (
                <p style={{ textAlign:'center', marginTop:'1.5rem', color:'#888' }}>
                    <a href="/login" style={{ color:'#00e676' }}>Accedi</a> per lasciare un feedback.
                </p>
            )}
        </div>
    );
}

// Mount del componente React
const domNode = document.getElementById('recensioni-root');
const reactRoot = ReactDOM.createRoot(domNode);
reactRoot.render(<RecensioniApp />);
